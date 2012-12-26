package com.gmail.altakey.mint;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.View;
import android.view.LayoutInflater;
import android.app.ProgressDialog;
import android.widget.TextView;
import java.util.Date;
import android.util.Log;
import java.io.IOException;

public class TaskPostFragment extends DialogFragment {
    private ToodledoClient mClient;
    private static final int DUE = 86400;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = new ToodledoClient(Authenticator.create(getActivity()), getActivity());
    }

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
            .setTitle("Post task here")
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
        private TextView mmField;

        public PostAction(TextView field) {
            mmField = field;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            new TaskAddTask(build()).execute();
        }

        private Task build() {
            final Task t = new Task();
            t.title = mmField.getText().toString();
            t.duedate = (new Date().getTime() + DUE * 1000) / 1000;
            return t;
        }
    }

    private class TaskAddTask extends NetworkTask {
        private Task mmTask;
        private VolatileDialog mmProgress = new Progress();

        public TaskAddTask(Task task) {
            mmTask = task;
        }

        @Override
        protected void onPreExecute() {
            mmProgress.show();
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
            mmProgress.dismiss();
        }

        private class Progress implements VolatileDialog {
            private Dialog mmmDialog;

            @Override
            public void show() {
                final ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setTitle("Adding task");
                dialog.setMessage("Adding task...");
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
