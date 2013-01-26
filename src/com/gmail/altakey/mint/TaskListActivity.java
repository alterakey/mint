package com.gmail.altakey.mint;

import com.example.android.swipedismiss.*;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.support.v4.content.LocalBroadcastManager;

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

    private ClientStatusReceiver mClientStatusReceiver = new ClientStatusReceiver();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plate);

        final Intent intent = getIntent();
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

    @Override
    public void onResume() {
        super.onResume();
        mClientStatusReceiver.register();
    }

    @Override
    public void onPause() {
        super.onPause();
        mClientStatusReceiver.unregister();
    }

    public static class TaskListFragment extends ListFragment
    {
        public static final String TAG = "task_list";

        private TaskListAdapter mAdapter;
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
            final TaskListFragment f = new TaskListFragment();
            final Bundle args = new Bundle();
            args.putString(KEY_LIST_FILTER, filter);
            f.setArguments(args);
            return f;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            final Context context = getActivity();
            final Bundle args = getArguments();

            mAdapter = new TaskListAdapterBuilder().build();
            mFilterType = args.getString(KEY_LIST_FILTER, "hotlist");

            final ListView listView = getListView();
            final SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                    listView,
                    new SwipeDismissListViewTouchListener.OnDismissCallback() {
                        @Override
                        public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                            for (int position : reverseSortedPositions) {
                                final Map<String, ?> e = (Map<String, ?>)mAdapter.getItem(position);
                                final Task task = (Task)e.get("task");

                                task.markAsDone();

                                final DB db = new DB(getActivity());
                                try {
                                    db.openForWriting();
                                    db.commitTask(task);
                                } finally {
                                    db.close();
                                }

                                final Intent intent = new Intent(context, ToodledoClientService.class);
                                intent.setAction(ToodledoClientService.ACTION_SYNC);
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
            inflater.inflate(R.menu.list, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
            case R.id.main_preferences:
                startActivity(new Intent(getActivity(), ConfigActivity.class));
                return false;
            case R.id.main_post:
                final DialogFragment f = new TaskPostFragment();
                f.setTargetFragment(this, 0);
                f.show(getFragmentManager(), "post_task");
                return false;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onResume() {
            super.onResume();
            reload();
            update();
        }

        @Override
        public void onListItemClick(ListView lv, View v, int position, long id) {
            super.onListItemClick(lv, v, position, id);
            final Map<String, ?> map = (Map<String, ?>)mAdapter.getItem(position);
            final Task task = (Task)map.get("task");
            final Intent intent = new Intent(getActivity(), TaskEditActivity.class);
            intent.putExtra(TaskEditActivity.KEY_TASK_ID, task._id);
            startActivity(intent);
        }

        private Authenticator getAuthenticator() {
            return Authenticator.create(getActivity());
        }

        public void reload() {
            mAdapter.reload();
        }

        private void reloadSilently() {
            mAdapter.reloadSilently();
        }

        private void refresh() {
            mAdapter.notifyDataSetChanged();
        }

        private void update() {
            final Context context = getActivity();
            final Intent intent = new Intent(context, ToodledoClientService.class);
            intent.setAction(ToodledoClientService.ACTION_SYNC);
            context.startService(intent);
        }

        public class TaskListAdapterBuilder {
            public TaskListAdapter build() {
                final List<Map<String, ?>> data = new LinkedList<Map<String, ?>>();
                final TaskListAdapter adapter = new TaskListAdapter(
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
                      new String[] { "title", "folder_0", "context_0", "due", "timer_flag" },
                      new int[] { R.id.list_task_title, R.id.list_task_folder_0, R.id.list_task_context_0, R.id.list_task_due, R.id.list_task_timer_flag });
                mmmData = data;
            }

            public void removeTask(Task t) {
                final Queue<Map<String, ?>> toBeRemoved = new LinkedList<Map<String, ?>>();
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
                new TaskListLoadTask(mmmData, false).execute();
            }

            public void reloadSilently() {
                mmmData.clear();
                new TaskListLoadTask(mmmData, true).execute();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = super.getView(position, convertView, parent);
                final View priority = convertView.findViewById(R.id.list_task_prio);
                final Map<String, ?> map = mmmData.get(position);
                final Task task = (Task)map.get("task");
                final Resources res = getResources();

                switch (((Long)map.get("priority")).intValue()) {
                case -1:
                    priority.setBackgroundColor(res.getColor(R.color.prio_negative));
                    break;
                case 0:
                    priority.setBackgroundColor(res.getColor(R.color.prio_low));
                    break;
                case 1:
                    priority.setBackgroundColor(res.getColor(R.color.prio_normal));
                    break;
                case 2:
                    priority.setBackgroundColor(res.getColor(R.color.prio_high));
                    break;
                case 3:
                default:
                    priority.setBackgroundColor(res.getColor(R.color.prio_top));
                    break;
                }

                if (null == map.get("folder_0")) {
                    convertView.findViewById(R.id.list_task_folder_0).setVisibility(View.GONE);
                } else {
                    convertView.findViewById(R.id.list_task_folder_0).setVisibility(View.VISIBLE);
                }
                if (null == map.get("context_0")) {
                    convertView.findViewById(R.id.list_task_context_0).setVisibility(View.GONE);
                } else {
                    convertView.findViewById(R.id.list_task_context_0).setVisibility(View.VISIBLE);
                }

                return convertView;
            }
        }

        private class TaskListLoadTask extends AsyncTask<Void, Void, List<Task>> {
            private VolatileDialog mmProgress;
            private List<Map<String, ?>> mmData;

            public TaskListLoadTask(List<Map<String, ?>> data, boolean silent) {
                mmData = data;
                if (silent) {
                    mmProgress = new NullProgress();
                } else {
                    mmProgress = new Progress();
                }
            }

            @Override
            protected void onPreExecute() {
                mmProgress.show();
            }

            @Override
            protected List<Task> doInBackground(Void... args) {
                final DB db = new DB(getActivity());
                try {
                    db.open();

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
                } finally {
                    db.close();
                }
            }

            @Override
            protected void onPostExecute(List<Task> ret) {
                for (Task t : ret) {
                    if (t.completed != 0)
                        continue;

                    final TaskContext c = t.resolved.context;
                    final TaskFolder f = t.resolved.folder;

                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("task", t);
                    map.put("title", t.title);
                    map.put("priority", t.priority);

                    if (!f.isNull()) {
                        map.put("folder_0", String.format("%s", f.name));
                    }
                    if (!c.isNull()) {
                        map.put("context_0", String.format("%s", c.name));
                    }

                    if (t.duedate > 0) {
                        map.put("due", new Formatter().format("%1$tY-%1$tm-%1$td", new Date(t.duedate * 1000)).toString());
                    }
                    //map.put("timer_flag", "(on)");
                    mmData.add(map);
                }

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

            private class NullProgress implements VolatileDialog {
                @Override
                public void show() {
                }

                @Override
                public void dismiss() {
                }
            }
        }
    }

    private class ClientStatusReceiver extends BroadcastReceiver {
        private final Activity mmActivity = TaskListActivity.this;

        public void register() {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(ToodledoClientService.ACTION_SYNC_DONE);
            LocalBroadcastManager.getInstance(mmActivity).registerReceiver(this, filter);
        }

        public void unregister() {
            LocalBroadcastManager.getInstance(mmActivity).unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ToodledoClientService.ACTION_SYNC_DONE.equals(action)) {
                poke();
            }
        }

        private void poke() {
            final TaskListFragment f = (TaskListFragment)getFragmentManager().findFragmentByTag(TaskListFragment.TAG);
            if (f != null) {
                f.reloadSilently();
            }
        }
    }
}
