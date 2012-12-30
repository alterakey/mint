package com.gmail.altakey.mint;

import com.example.android.swipedismiss.*;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
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
        setTitle(new TaskListFragment.Filter(filter).getTitle());

        getFragmentManager()
            .beginTransaction()
            .add(R.id.frag, TaskListFragment.newInstance(filter), TaskListFragment.TAG)
            .commit();
    }

    public static class TaskListFragment extends ListFragment
    {
        public static final String TAG = "task_list";

        private TaskListAdapter mAdapter;
        private ToodledoClient mClient;
        private String mFilterType;

        public static class Filter {
            private static final String[] ALL = { "hotlist", "inbox", "next_action", "reference", "waiting", "someday" };
            private static final String[] TITLES = { "Hotlist", "Inbox", "Next Action", "Reference", "Waiting", "Someday" };

            private String mmFilter;

            public Filter(String filter) {
                mmFilter = filter;
            }

            public List<String> getTitles() {
                return Arrays.asList(TITLES);
            }

            public String getTitle() {
                try {
                    return TITLES[Arrays.asList(ALL).indexOf(mmFilter)];
                } catch (IndexOutOfBoundsException e) {
                    return "?";
                }
            }
        }

        public String getFilter() {
            return mFilterType;
        }

        public static TaskListFragment newInstance(String filter) {
            TaskListFragment f = new TaskListFragment();
            Bundle args = new Bundle();
            args.putString(KEY_LIST_FILTER, filter);
            f.setArguments(args);
            return f;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            final Context context = getActivity();
            Bundle args = getArguments();

            mAdapter = new TaskListAdapterBuilder().build();
            mClient = new ToodledoClient(null, getActivity());
            mFilterType = args.getString(KEY_LIST_FILTER, "hotlist");

            ListView listView = getListView();
            SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                    listView,
                    new SwipeDismissListViewTouchListener.OnDismissCallback() {
                        @Override
                        public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                            for (int position : reverseSortedPositions) {
                                final Map<String, ?> e = (Map<String, ?>)mAdapter.getItem(position);
                                final Task task = (Task)e.get("task");
                                final Intent intent = new Intent(context, ToodledoClientService.class);
                                intent.setAction(ToodledoClientService.ACTION_COMPLETE);
                                intent.putExtra(ToodledoClientService.EXTRA_TASKS, ToodledoClientService.asListOfTasks((Task)e.get("task")));
                                context.startService(intent);
                                mAdapter.remove(position);
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    });
            listView.setOnTouchListener(touchListener);
            listView.setOnScrollListener(touchListener.makeScrollListener());

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
            final Map<String, ?> map = (Map<String, ?>)mAdapter.getItem(position);
            final Task task = (Task)map.get("task");
            final Intent intent = new Intent(getActivity(), TaskEditActivity.class);
            intent.putExtra(TaskEditActivity.KEY_TASK_ID, task.id);
            startActivity(intent);
        }

        private Authenticator getAuthenticator() {
            return Authenticator.create(getActivity());
        }

        public void reload() {
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

            public TaskListAdapter(Context ctx, List<Map<String, ?>> data) {
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

            public void remove(int pos) {
                mmmData.remove(pos);
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
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.finish();
                }
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

        private class TaskListLoadTask extends AsyncTask<Void, Void, Void> {
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
            protected Void doInBackground(Void... args) {
                DB db = new DB(getActivity());
                try {
                    db.open();

                    for (Task t : getTasks(db)) {
                        if (t.completed != 0)
                            continue;

                        TaskContext c = t.resolved.context;
                        TaskFolder f = t.resolved.folder;

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
                    return null;
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
                } else {
                    final int status = new DB.Filter(mFilterType).getStatus();

                    if (status == DB.Filter.UNKNOWN) {
                        return new LinkedList<Task>();
                    } else {
                        return db.getTasks(String.format("status=\"%d\" and completed=0", status), DB.DEFAULT_ORDER);
                    }
                }
            }

            @Override
            protected void onPostExecute(Void ret) {
                mmProgress.dismiss();
                refresh();
            }

            private class Progress implements VolatileDialog {
                private Dialog mmmDialog;

                @Override
                public void show() {
                    final ProgressDialog dialog = new ProgressDialog(getActivity());
                    dialog.setTitle("Getting tasks");
                    dialog.setMessage("Querying tasks...");
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
    }
}
