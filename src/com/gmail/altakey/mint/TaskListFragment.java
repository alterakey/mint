package com.gmail.altakey.mint;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
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
import java.text.SimpleDateFormat;

public class TaskListFragment extends ListFragment
{
    public static final String TAG = "task_list";
    public static final String ARG_FILTER = "filter";

    private TaskListAdapter mAdapter;
    private FilterType mFilterType;
    private ActionMode mActionMode;

    private TaskLoaderManipulator mTaskLoaderManip = new TaskLoaderManipulator();

    public FilterType getFilter() {
        return mFilterType;
    }

    public static TaskListFragment newInstance(FilterType filter) {
        final TaskListFragment f = new TaskListFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_FILTER, filter);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Context context = getActivity();
        final Bundle args = getArguments();

        mAdapter = new TaskListAdapter(getActivity(), null);
        mFilterType = (FilterType)args.getParcelable(ARG_FILTER);

        getActivity().setTitle(mFilterType.getTitle());

        final ListView listView = getListView();
        listView.setOnItemLongClickListener(new SelectionModeListener());

        setHasOptionsMenu(true);
        setListAdapter(mAdapter);
        setListShown(false);
        getLoaderManager().initLoader(1, null, mTaskLoaderManip);
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
        getLoaderManager().restartLoader(1, null, mTaskLoaderManip);
        update();
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        super.onListItemClick(lv, v, position, id);
        final Intent intent = new Intent(getActivity(), TaskEditActivity.class);
        intent.putExtra(TaskEditActivity.KEY_TASK_ID, id);
        startActivity(intent);
    }

    private void update() {
        final Context context = getActivity();
        final Intent intent = new Intent(context, ToodledoClientService.class);
        intent.setAction(ToodledoClientService.ACTION_SYNC);
        context.startService(intent);
    }

    private class SelectionModeListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
            if (mActionMode != null) {
                return false;
            }

            getActivity().startActionMode(new TaskSelectionMode());
            v.setSelected(true);
            return true;
        }
    }

    private class TaskListAdapter extends CursorAdapter {
        public TaskListAdapter(Context context, Cursor c) {
            super(context, c);
        }

        public TaskListAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        public TaskListAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View v = inflater.inflate(R.layout.list_item, parent, false);
            bindView(v, context, cursor);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final TextView title = (TextView)view.findViewById(R.id.list_task_title);
            final TextView taskFolder = (TextView)view.findViewById(R.id.list_task_folder_0);
            final TextView taskContext = (TextView)view.findViewById(R.id.list_task_context_0);
            final TextView due = (TextView)view.findViewById(R.id.list_task_due);
            final TextView timerFlag = (TextView)view.findViewById(R.id.list_task_timer_flag);
            final View priority = view.findViewById(R.id.list_task_prio);
            final Task task = Task.fromCursor(cursor, 0);
            final Resources res = getResources();

            title.setText(task.title);
            due.setText(new SimpleDateFormat("yyyy/MM/dd").format(task.duedate));
            timerFlag.setText("");

            switch ((int)task.priority) {
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

            if (task.resolved.folder == null) {
                taskFolder.setVisibility(View.GONE);
            } else {
                taskFolder.setText(task.resolved.folder.name);
                taskFolder.setVisibility(View.VISIBLE);
            }
            if (task.resolved.context == null) {
                taskContext.setVisibility(View.GONE);
            } else {
                taskContext.setText(task.resolved.context.name);
                taskContext.setVisibility(View.VISIBLE);
            }
        }
    }

    private class TaskLoaderManipulator implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            getActivity().setProgressBarIndeterminateVisibility(true);
            setListShown(false);
            return new CursorLoader(
                getActivity(),
                TaskProvider.CONTENT_URI,
                TaskProvider.PROJECTION,
                mFilterType.getSelection(),
                mFilterType.getSelectionArgs(),
                TaskProvider.DEFAULT_ORDER
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            mAdapter.changeCursor(data);
            setListShown(true);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.changeCursor(null);
        }
    }

    private class TaskSelectionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;

            final MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.list_selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.list_selection_delete:
                Toast.makeText(getActivity(), "TBD: remove tasks", Toast.LENGTH_SHORT).show();
                mode.finish();
                return true;
            default:
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    }
}
