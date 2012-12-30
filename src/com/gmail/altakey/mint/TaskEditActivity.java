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
            return inflater.inflate(R.layout.edit, root, false);
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
            } finally {
                if (db != null) {
                    db.close();
                }
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.main, menu);
        }

        @Override
        public void onResume() {
            super.onResume();
            mClient.setAuthenticator(getAuthenticator());
            reload();
        }

        private void reload() {
        }

        private void refresh() {
        }

        private Authenticator getAuthenticator() {
            return Authenticator.create(getActivity());
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

        private class TaskEditTask extends ReportingNetworkTask {
            private VolatileDialog mmProgress = new Progress();
            private List<Map<String, ?>> mmData;

            public TaskEditTask(List<Map<String, ?>> data) {
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
                } finally {
                    if (db != null) {
                        db.close();
                    }
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
                    dialog.setTitle("Updating tasks");
                    dialog.setMessage("Updating remote tasks...");
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
