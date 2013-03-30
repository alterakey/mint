package com.gmail.altakey.mint;

import com.example.android.swipedismiss.*;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.content.Loader;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.app.LoaderManager;
import android.database.Cursor;
import android.content.CursorLoader;

import java.io.IOException;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Arrays;
import java.util.Calendar;

public class TaskEditActivity extends Activity
{
    public static final String KEY_TASK_ID = "task_id";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plate);

        final Intent intent = getIntent();
        final long task = intent.getLongExtra(KEY_TASK_ID, -1);

        getFragmentManager()
            .beginTransaction()
            .add(R.id.frag, TaskEditFragment.newInstance(task), TaskEditFragment.TAG)
            .commit();
    }

    public static class TaskEditFragment extends Fragment
    {
        public static final String TAG = "task_edit";

        private static final int REQ_SET_DATE = 1;
        private static final int REQ_SET_TIME = 2;

        private Task mTask;
        private TaskLoaderManipulator mLoaderManip = new TaskLoaderManipulator();

        public static TaskEditFragment newInstance(long task) {
            final TaskEditFragment f = new TaskEditFragment();
            final Bundle args = new Bundle();
            args.putLong(KEY_TASK_ID, task);
            f.setArguments(args);
            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
            final View v = inflater.inflate(R.layout.edit, root, false);
            v.findViewById(R.id.due).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment f = new DueDatePicker();
                    f.setTargetFragment(TaskEditFragment.this, REQ_SET_DATE);
                    f.show(getFragmentManager(), "datePicker");
                }
            });
            update(v);

            getLoaderManager().initLoader(1, null, mLoaderManip);
            return v;
        }

        private void update(View v) {
            final TextView title = (TextView)v.findViewById(R.id.title);
            final TextView note = (TextView)v.findViewById(R.id.note);
            final TextView due = (TextView)v.findViewById(R.id.due);
            if (mTask != null) {
                title.setText(mTask.title);
                note.setText(mTask.note);
                if (mTask.duedate == 0) {
                    due.setText("(not set, tap to edit)");
                } else {
                    if (mTask.duetime == 0) {
                        due.setText(new Formatter().format("%1$tY-%1$tm-%1$td", new Date(mTask.duedate * 1000)).toString());
                    } else {
                        due.setText(new Formatter().format("%1$tY-%1$tm-%1$td %2$tH:%2$tM", new Date(mTask.duedate * 1000), new Date(mTask.duetime * 1000)).toString());
                    }
                }
            }
        }

        private void commit(View v) {
            final TextView title = (TextView)v.findViewById(R.id.title);
            final TextView note = (TextView)v.findViewById(R.id.note);
            if (mTask != null) {
                mTask.title = title.getText().toString();
                mTask.note = note.getText().toString();
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == TaskEditFragment.REQ_SET_DATE) {
                final int[] yearMonthDay = data.getIntArrayExtra(DueDatePicker.EXTRA_DATE);
                final Date date = new Date(
                    yearMonthDay[0] - 1900,
                    yearMonthDay[1],
                    yearMonthDay[2]
                );

                mTask.duedate = date.getTime() / 1000;
                update(getView());
            } else if (requestCode == TaskEditFragment.REQ_SET_TIME) {
                final int[] hourMinute = data.getIntArrayExtra(DueTimePicker.EXTRA_TIME);
                final Date date = new Date(
                    2012, 12, 10,
                    hourMinute[0],
                    hourMinute[1]
                );

                mTask.duetime = date.getTime() / 1000;
                update(getView());
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.main, menu);
        }

        @Override
        public void onPause() {
            super.onPause();
            if (mTask != null) {
                commit(getView());
                getLoaderManager().initLoader(2, null, new TaskCommitterManipulator());
            }
        }

        private class TaskLoaderManipulator implements LoaderManager.LoaderCallbacks<Cursor> {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getActivity(),
                                        TaskProvider.CONTENT_URI,
                                        TaskProvider.PROJECTION,
                                        TaskProvider.ID_FILTER,
                                        new String[] { },
                                        null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mTask = Task.fromCursor(data, 0);
                update(getView());
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        }

        private class TaskCommitterManipulator implements LoaderManager.LoaderCallbacks<Void> {
            @Override
            public Loader<Void> onCreateLoader(int id, Bundle args) {
                return new AsyncTaskLoader<Void>(getActivity()) {
                    @Override
                    public Void loadInBackground() {
                        final Task task = mTask;
                        final ContentValues values = new ContentValues();
                        values.put(TaskProvider.COLUMN_TITLE, task.title);
                        values.put(TaskProvider.COLUMN_NOTE, task.note);
                        values.put(TaskProvider.COLUMN_MODIFIED, task.modified);
                        values.put(TaskProvider.COLUMN_COMPLETED, task.completed);
                        values.put(TaskProvider.COLUMN_FOLDER, task.folder);
                        values.put(TaskProvider.COLUMN_CONTEXT, task.context);
                        values.put(TaskProvider.COLUMN_PRIORITY, task.priority);
                        values.put(TaskProvider.COLUMN_STAR, task.star);
                        values.put(TaskProvider.COLUMN_DUEDATE, task.duedate);
                        values.put(TaskProvider.COLUMN_DUETIME, task.duetime);
                        values.put(TaskProvider.COLUMN_STATUS, task.status);
                        getContext().getContentResolver().update(TaskProvider.CONTENT_URI, values, TaskProvider.ID_FILTER, new String[] { String.valueOf(task.id) });
                        return null;
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Void> loader, Void data) {
                final Intent intent = new Intent(getActivity(), ToodledoClientService.class);
                intent.setAction(ToodledoClientService.ACTION_SYNC);
                getActivity().startService(intent);
            }

            @Override
            public void onLoaderReset(Loader<Void> loader) {
            }
        }
    }

    public static class DueDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        private static final String EXTRA_DATE = "date";

        // https://code.google.com/p/android/issues/detail?id=34860
        private boolean mmFired = false;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            final int year = c.get(Calendar.YEAR);
            final int month = c.get(Calendar.MONTH);
            final int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            if (mmFired == false) {
                mmFired = true;
                callback(year, month, day);

                final DialogFragment f = new DueTimePicker();
                f.setTargetFragment(getTargetFragment(), TaskEditFragment.REQ_SET_TIME);
                f.show(getFragmentManager(), "duetime");
            }
        }

        private void callback(int year, int month, int day) {
            final Intent intent = new Intent();
            intent.putExtra(EXTRA_DATE, new int[] { year, month, day });
            getTargetFragment().onActivityResult(getTargetRequestCode(), 0, intent);
        }
    }

    public static class DueTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        private static final String EXTRA_TIME = "time";

        // https://code.google.com/p/android/issues/detail?id=34860
        private boolean mmFired = false;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            final int hour = c.get(Calendar.HOUR_OF_DAY);
            final int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }

        @Override
        public void onTimeSet(TimePicker view, int hour, int minute) {
            if (mmFired == false) {
                mmFired = true;
                callback(hour, minute);
            }
        }

        private void callback(int hour, int minute) {
            final Intent intent = new Intent();
            intent.putExtra(EXTRA_TIME, new int[] { hour, minute });
            getTargetFragment().onActivityResult(getTargetRequestCode(), 0, intent);
        }
    }

}
