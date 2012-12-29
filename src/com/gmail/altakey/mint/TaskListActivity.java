package com.gmail.altakey.mint;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.IOException;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Arrays;

public class TaskListActivity extends Activity
{
    public static final String KEY_LIST_FILTER = "filter";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plate);

        Intent intent = getIntent();
        String filter = intent.getStringExtra(KEY_LIST_FILTER);
        if (filter == null) {
            filter = "hotlist";
        }
        setTitle(TaskListFragment.getTitle(filter));

        getFragmentManager()
            .beginTransaction()
            .add(R.id.frag, TaskListFragment.newInstance(filter), TaskListFragment.TAG)
            .commit();
    }

    private static class TaskListFragment extends ListFragment
    {
        public static final String TAG = "task_list";
        public static final String[] TITLES = { "Hotlist", "Inbox", "Next Action", "Reference", "Waiting", "Someday" };
        public static final String[] FILTERS = { "hotlist", "inbox", "next_action", "reference", "waiting", "someday" };

        private TaskListAdapter mAdapter;
        private ToodledoClient mClient;
        private String mFilterType;

        public static TaskListFragment newInstance(String filter) {
            TaskListFragment f = new TaskListFragment();
            Bundle args = new Bundle();
            args.putString(KEY_LIST_FILTER, filter);
            f.setArguments(args);
            return f;
        }

        public static String getTitle(String filter) {
            try {
                return TITLES[Arrays.asList(FILTERS).indexOf(filter)];
            } catch (IndexOutOfBoundsException e) {
                return "?";
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();

            mAdapter = new TaskListAdapterBuilder().build();
            mClient = new ToodledoClient(null, getActivity());
            mFilterType = args.getString(KEY_LIST_FILTER, "hotlist");

            setHasOptionsMenu(true);
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
                new TaskPostFragment().show(getFragmentManager(), "post_task");
                return false;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onResume() {
            super.onResume();
            mClient.setAuthenticator(getAuthenticator());
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

        private abstract class ReportingNetworkTask extends NetworkTask {
            @Override
            protected void onLoginRequired() {
                abortWithErrorType(LoginTroubleActivity.TYPE_REQUIRED);
            }

            @Override
            protected void onLoginFailed() {
                abortWithErrorType(LoginTroubleActivity.TYPE_FAILED);
            }

            protected void abort() {
                getActivity().finish();
            }

            protected void abortWithErrorType(String type) {
                Intent intent = new Intent(getActivity(), LoginTroubleActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(LoginTroubleActivity.KEY_TYPE, type);
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0);
                getActivity().finish();
                getActivity().overridePendingTransition(0, 0);
            }

            @Override
            protected void onLoginError() {
                super.onLoginError();
                Toast.makeText(getActivity(), String.format("fetch failure: %s", mError.getMessage()), Toast.LENGTH_LONG).show();
            }
        }

        private class TaskListLoadTask extends ReportingNetworkTask {
            private VolatileDialog mmProgress = new Progress();
            private List<Map<String, ?>> mmData;

            public TaskListLoadTask(List<Map<String, ?>> data) {
                mmData = data;
            }

            @Override
            protected void onPreExecute() {
                mmProgress.show();
            }

            @Override
            protected void doTask() throws IOException, Authenticator.Exception {
                DB db = new DB(getActivity());
                try {
                    db.open();
                    db.update(mClient);

                    for (Task t : getTasks(db)) {
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

            private List<Task> getTasks(DB db) {
                Log.d("TLT", String.format("loading %s", mFilterType));
                if ("hotlist".equals(mFilterType)) {
                    return db.getHotTasks();
                } else if ("inbox".equals(mFilterType)) {
                    return db.getTasks("status=\"0\" and completed=0", DB.DEFAULT_ORDER);
                } else if ("next_action".equals(mFilterType)) {
                    return db.getTasks("status=\"1\" and completed=0", DB.DEFAULT_ORDER);
                } else if ("reference".equals(mFilterType)) {
                    return db.getTasks("status=\"10\" and completed=0", DB.DEFAULT_ORDER);
                } else if ("waiting".equals(mFilterType)) {
                    return db.getTasks("status=\"5\" and completed=0", DB.DEFAULT_ORDER);
                } else if ("someday".equals(mFilterType)) {
                    return db.getTasks("status=\"8\" and completed=0", DB.DEFAULT_ORDER);
                } else {
                    return new LinkedList<Task>();
                }
            }

            @Override
            protected void onPostExecute(Integer ret) {
                super.onPostExecute(ret);
                mmProgress.dismiss();
                if (ret == OK) {
                    refresh();
                }
            }

            @Override
            protected void onCancelled() {
                abort();
            }

            private class Progress implements VolatileDialog {
                private Dialog mmmDialog;

                @Override
                public void show() {
                    final ProgressDialog dialog = new ProgressDialog(getActivity());
                    dialog.setTitle("Getting tasks");
                    dialog.setMessage("Querying remote tasks...");
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setIndeterminate(true);
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            cancel(true);
                        }
                    });
                    mmmDialog = dialog;
                    mmmDialog.show();
                }

                @Override
                public void dismiss() {
                    mmmDialog.dismiss();
                }
            }
        }

        private class TaskCompleteTask extends ReportingNetworkTask {
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
    }
}
