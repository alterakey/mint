package com.gmail.altakey.mint;

import com.example.android.swipedismiss.*;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
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

import java.io.IOException;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Arrays;

public class TaskEditActivity extends Activity
{
    public static final String KEY_TASK_ID = "task_id";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plate);

        Intent intent = getIntent();
        long task = intent.getLongExtra(KEY_TASK_ID, -1);

        getFragmentManager()
            .beginTransaction()
            .add(R.id.frag, TaskEditFragment.newInstance(task), TaskEditFragment.TAG)
            .commit();
    }

    public static class TaskEditFragment extends Fragment
    {
        public static final String TAG = "task_edit";

        private ToodledoClient mClient;
        private Task mTask;

        public static TaskEditFragment newInstance(long task) {
            TaskEditFragment f = new TaskEditFragment();
            Bundle args = new Bundle();
            args.putLong(KEY_TASK_ID, task);
            f.setArguments(args);
            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.edit, root, false);
            update(v);
            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle args = getArguments();

            mClient = new ToodledoClient(null, getActivity());

            DB db = null;
            try {
                db = new DB(getActivity());
                db.open();
                mTask = db.getTask(args.getLong(KEY_TASK_ID));
                update(getView());
            } finally {
                if (db != null) {
                    db.close();
                }
            }
        }

        private void update(View v) {
            final TextView title = (TextView)v.findViewById(R.id.title);
            if (mTask != null) {
                title.setText(mTask.title);
            }
        }

        private void commit(View v) {
            final TextView title = (TextView)v.findViewById(R.id.title);
            if (mTask != null) {
                mTask.title = title.getText().toString();
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.main, menu);
        }

        @Override
        public void onPause() {
            super.onPause();
            commit(getView());
            final Intent intent = new Intent(getActivity(), ToodledoClientService.class);
            intent.setAction(ToodledoClientService.ACTION_COMMIT);
            intent.putExtra(ToodledoClientService.EXTRA_TASKS, ToodledoClientService.asListOfTasks(mTask));
            getActivity().startService(intent);
        }
    }
}
