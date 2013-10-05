package com.gmail.altakey.mint;

import android.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.util.Date;
import java.io.IOException;

public class TaskPostFragment extends DialogFragment {
    private static final int DUE = 86400;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            .setPositiveButton(android.R.string.ok, new PostAction(field));
        return builder.create();
    }

    private class PostAction implements DialogInterface.OnClickListener {
        private TextView mmField;

        public PostAction(TextView field) {
            mmField = field;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            final Context context = getActivity();
            final Task task = build();

            addTask(context, task);

            final Intent intent = new Intent(context, ToodledoClientService.class);
            intent.setAction(ToodledoClientService.ACTION_SYNC);
            context.startService(intent);
        }

        private void addTask(final Context context, final Task task) {
            final ContentValues values = new ContentValues();
            values.put(TaskProvider.COLUMN_COOKIE, Task.nextCookie());
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
            context.getContentResolver().insert(TaskProvider.CONTENT_URI, values);
        }

        private Task build() {
            final Task t = new Task();
            final int status = getActiveFilter().getToodledoStatus();
            t.title = mmField.getText().toString();
            if (status == FilterType.UNKNOWN) {
                t.duedate = (new Date().getTime() + DUE * 1000) / 1000;
            } else {
                t.status = String.valueOf(status);
            }
            return t;
        }

        private FilterType getActiveFilter() {
            final TaskListFragment f = (TaskListFragment)getTargetFragment();
            if (f == null) {
                return null;
            } else {
                return f.getFilter();
            }
        }
    }
}
