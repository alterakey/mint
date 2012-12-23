package com.gmail.altakey.mint;

import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.content.Intent;

import java.util.List;
import java.util.LinkedList;
import java.io.IOException;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;

import java.util.Date;
import java.util.Formatter;
import java.util.Queue;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.support.v4.app.DialogFragment;
import android.app.Dialog;

public class TaskListFragment extends ListFragment
{
    private TaskListAdapter mAdapter;
    private ToodledoClient mClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = new ToodledoClient(getAuthenticator(), getActivity());
        mAdapter = new TaskListAdapterBuilder().build();

        setHasOptionsMenu(true);
        getActivity().setTitle("Hotlist");
        setListAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.main_preferences:
            startActivity(new Intent(getActivity(), ConfigActivity.class));
            return false;
        case R.id.main_post:
            new PostTaskDialog().show(getFragmentManager(), "post_task");
            return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        super.onListItemClick(lv, v, position, id);
        final TaskListAdapter adapter = (TaskListAdapter)getListAdapter();
        final Map<String, ?> e = (Map<String, ?>)adapter.getItem(position);
        new TaskCompleteTask((Task)e.get("task")).execute();
    }

    private Authenticator getAuthenticator() {
        return Authenticator.create(getActivity());
    }

    private void reload() {
        mAdapter.reload();
    }

    private void refresh() {
        mAdapter.notifyDataSetChanged();
    }

    public class TaskListAdapterBuilder {
        public TaskListAdapter build() {
            final List<Map<String, ?>> data = new LinkedList<Map<String, ?>>();
            TaskListAdapter adapter = new TaskListAdapter(
                getActivity(),
                data
            );
            return adapter;
        }
    }

    private class TaskListAdapter extends SimpleAdapter {
        private List<Map<String, ?>> mmmData;

        public TaskListAdapter(android.content.Context ctx, List<Map<String, ?>> data) {
            super(ctx,
                  data,
                  R.layout.list_item,
                  new String[] { "title", "context_0", "context_1", "context_2", "due", "timer_flag" },
                  new int[] { R.id.list_task_title, R.id.list_task_context_0, R.id.list_task_context_1, R.id.list_task_context_2, R.id.list_task_due, R.id.list_task_timer_flag });
            mmmData = data;
        }

        public void removeTask(Task t) {
            Queue<Map<String, ?>> toBeRemoved = new LinkedList<Map<String, ?>>();
            for (Map<String, ?> e: mmmData) {
                if (e.get("task") == t) {
                    toBeRemoved.add(e);
                }
            }
            for (Map<String, ?> e: toBeRemoved) {
                mmmData.remove(e);
            }
        }

        public void reload() {
            mmmData.clear();
            new TaskListLoadTask(mmmData).execute();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            View priority = convertView.findViewById(R.id.list_task_prio);
            Map<String, ?> map = mmmData.get(position);
            final Task task = (Task)map.get("task");

            switch (((Long)map.get("priority")).intValue()) {
            case -1:
                priority.setBackgroundColor(0xff0000ff);
                break;
            case 0:
                priority.setBackgroundColor(0xff00ff00);
                break;
            case 1:
                priority.setBackgroundColor(0xffffff00);
                break;
            case 2:
                priority.setBackgroundColor(0xffff8800);
                break;
            case 3:
            default:
                priority.setBackgroundColor(0xffff0000);
                break;
            }
            if (task.grayedout) {
                convertView.setBackgroundColor(0x80ffffff);
            } else {
                convertView.setBackgroundColor(0x00000000);
            }
            return convertView;
        }
    }

    private abstract class NetworkTask extends AsyncTask<Void, Void, Integer> {
        protected Exception mmError;

        protected static final int OK = 0;
        protected static final int LOGIN_REQUIRED = 1;
        protected static final int LOGIN_FAILED = 2;
        protected static final int FAILURE = 3;

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                doTask();
                return OK;
            } catch (IOException e) {
                mmError = e;
                return FAILURE;
            } catch (Authenticator.BogusException e) {
                mmError = e;
                return LOGIN_REQUIRED;
            } catch (Authenticator.FailureException e) {
                mmError = e;
                return LOGIN_FAILED;
            } catch (Authenticator.Exception e) {
                mmError = e;
                return FAILURE;
            }
        }

        abstract protected void doTask() throws IOException, Authenticator.Exception;

        @Override
        protected void onPostExecute(Integer ret) {
            if (ret == LOGIN_REQUIRED) {
                showLoginRequired();
            } else if (ret == LOGIN_FAILED) {
                showLoginFailed();
            } else if (ret == FAILURE) {
                Log.e("TLF", "fetch failure", mmError);
                Toast.makeText(getActivity(), String.format("fetch failure: %s", mmError.getMessage()), Toast.LENGTH_LONG).show();
            }
        }

        private void showLoginRequired() {
            getFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new LoginRequiredFragment())
                .commit();
        }

        private void showLoginFailed() {
            getFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new LoginFailedFragment())
                .commit();
        }
    }

    private class TaskListLoadTask extends NetworkTask {
        private ProgressDialog mmDialog;
        private List<Map<String, ?>> mmData;

        public TaskListLoadTask(List<Map<String, ?>> data) {
            mmData = data;
        }

        @Override
        protected void onPreExecute() {
            mmDialog = new ProgressDialog(getActivity());
            mmDialog.setTitle("Getting tasks");
            mmDialog.setMessage("Querying remote tasks...");
            mmDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mmDialog.setIndeterminate(true);
            mmDialog.setCancelable(true);
            mmDialog.setCanceledOnTouchOutside(true);
            mmDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
            mmDialog.show();
        }

        @Override
        protected void doTask() throws IOException, Authenticator.Exception {
            DB db = new DB(getActivity());
            try {
                db.open();
                db.update(mClient);

                for (Task t : db.getHotTasks()) {
                    if (t.completed != 0)
                        continue;

                    Context c = t.resolved.context;
                    Folder f = t.resolved.folder;

                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("task", t);
                    map.put("title", t.title);
                    map.put("priority", t.priority);

                    if (f != null) {
                        map.put("context_0", String.format("%s", f.name));
                    }
                    if (c != null) {
                        map.put("context_1", String.format("@%s", c.name));
                    }

                    if (t.duedate > 0) {
                        map.put("due", new Formatter().format("%1$tY-%1$tm-%1$td", new Date(t.duedate * 1000)).toString());
                    }
                    //map.put("timer_flag", "(on)");
                    mmData.add(map);
                }
            } finally {
                if (db != null) {
                    db.close();
                }
            }
        }

        @Override
        protected void onPostExecute(Integer ret) {
            super.onPostExecute(ret);
            mmDialog.dismiss();
            if (ret == OK) {
                refresh();
            }
        }

        @Override
        protected void onCancelled() {
            refresh();
        }
    }

    private class TaskCompleteTask extends NetworkTask {
        private Task mmTask;

        public TaskCompleteTask(Task task) {
            mmTask = task;
        }

        @Override
        protected void onPreExecute() {
            startStrikeout(mmTask);
            refresh();
        }

        @Override
        protected void doTask() throws IOException, Authenticator.Exception {
            DB db = new DB(getActivity());
            try {
                mmTask.markAsDone();
                mClient.updateDone(mmTask);

                db.open();
                db.update(mClient);
            } finally {
                if (db != null) {
                    db.close();
                }
            }
        }

        @Override
        protected void onPostExecute(Integer ret) {
            super.onPostExecute(ret);
            if (ret == OK) {
                completeStrikeout(mmTask);
                refresh();
            } else if (ret == FAILURE) {
                stopStrikeout(mmTask);
                refresh();
            }
        }

        private void completeStrikeout(Task t) {
            mAdapter.removeTask(t);
        }

        private void startStrikeout(Task t) {
            t.grayedout = true;
        }

        private void stopStrikeout(Task t) {
            t.grayedout = false;
        }

    }

    private class TaskAddTask extends NetworkTask {
        private Task mmTask;
        private ProgressDialog mmDialog;

        public TaskAddTask(Task task) {
            mmTask = task;
        }

        @Override
        protected void onPreExecute() {
            mmDialog = new ProgressDialog(getActivity());
            mmDialog.setTitle("Adding task");
            mmDialog.setMessage("Adding task...");
            mmDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mmDialog.setIndeterminate(true);
            mmDialog.setCancelable(true);
            mmDialog.setCanceledOnTouchOutside(true);
            mmDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
            mmDialog.show();
        }

        @Override
        protected void doTask() throws IOException, Authenticator.Exception {
            DB db = new DB(getActivity());
            try {
                mClient.addTask(mmTask, null);

                db.open();
                db.update(mClient);
            } finally {
                if (db != null) {
                    db.close();
                }
            }
        }

        @Override
        protected void onPostExecute(Integer ret) {
            super.onPostExecute(ret);
            mmDialog.dismiss();
            reload();
        }
    }

    private class PostTaskDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final LayoutInflater inflater = getActivity().getLayoutInflater();
            final View layout = inflater.inflate(
                R.layout.post_task,
                null);
            final TextView field = (TextView)layout.findViewById(R.id.title);

            builder
                .setView(layout)
                .setTitle("Post task")
                .setOnCancelListener(new CancelAction())
                .setPositiveButton(android.R.string.ok, new PostAction(field));
            return builder.create();
        }

        private class CancelAction implements DialogInterface.OnCancelListener {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        }

        private class PostAction implements DialogInterface.OnClickListener {
            private TextView mmmField;
            private static final int DUE = 86400;

            public PostAction(TextView field) {
                mmmField = field;
            }

            @Override
            public void onClick(DialogInterface dialog, int which) {
                new TaskAddTask(build()).execute();
            }

            private Task build() {
                final Task t = new Task();
                t.title = mmmField.getText().toString();
                t.duedate = (new Date().getTime() + DUE * 1000) / 1000;
                return t;
            }
        }
    }
}
