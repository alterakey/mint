package com.gmail.altakey.mint.timer;

import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.util.Log;

public class TimerFragment extends Fragment {
    public static final String TAG = "timer";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_SECOND = "second";

    private TickReceiver mTickReceiver = new TickReceiver();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.timer, container, false);
        if (savedInstanceState != null) {
            ((TextView)v.findViewById(R.id.min)).setText(savedInstanceState.getCharSequence(KEY_MINUTE));
            ((TextView)v.findViewById(R.id.sec)).setText(savedInstanceState.getCharSequence(KEY_SECOND));
        }

        final View timer = v.findViewById(R.id.timer);
        timer.setOnClickListener(new TimerClickListener());
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final View root = getView();
        outState.putCharSequence(KEY_MINUTE, ((TextView)root.findViewById(R.id.min)).getText());
        outState.putCharSequence(KEY_SECOND, ((TextView)root.findViewById(R.id.sec)).getText());
    }

    @Override
    public void onStop() {
        super.onStop();
        mTickReceiver.detach();
    }

    @Override
    public void onStart() {
        super.onStart();
        new TimerUpdater().refresh();
        mTickReceiver.attach();
    }

    private class TimerClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            new TimerToggler().toggle();
        }
    }

    private class TimerToggler {
        public void toggle() {
            final int state = TimerService.getState();
            if (state == TimerService.STATE_RESET) {
                controlService(TimerService.ACTION_START);
            } else {
                new TimerResetConfirmFragment().show(getFragmentManager(), TimerResetConfirmFragment.TAG);
            }
        }

        private void controlService(String action) {
            final Context c = getActivity();
            if (c != null) {
                final Intent intent = new Intent(c, TimerService.class);
                intent.setAction(action);
                c.startService(intent);
            }
        }
    }

    public static class TimerResetConfirmFragment extends DialogFragment {
        public static final String TAG = "timer_reset_confirmation";

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                .setMessage("Are you sure to reset?")
                .setPositiveButton(android.R.string.yes, new ConfirmAction())
                .setNegativeButton(android.R.string.no, new NullAction())
                .create();
        }

        private void controlService(String action) {
            final Context c = getActivity();
            if (c != null) {
                final Intent intent = new Intent(c, TimerService.class);
                intent.setAction(action);
                c.startService(intent);
            }
        }

        private class ConfirmAction implements DialogInterface.OnClickListener {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                controlService(TimerService.ACTION_RESET);
            }
        }

        private class NullAction implements DialogInterface.OnClickListener {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }
    }

    private class TimerUpdater {
        public void refresh() {
            long remaining = TimerService.getRemaining(TimerService.getDueMillis());
            remaining = ((long)Math.ceil(remaining / 1000.0)) * 1000;

            final long seconds = remaining / 1000 % 60;
            final long minutes = remaining / 60000;

            final View root = getView();
            if (root != null) {
                ((TextView)root.findViewById(R.id.min)).setText(String.format("%02d", minutes));
                ((TextView)root.findViewById(R.id.sec)).setText(String.format("%02d", seconds));
            }
        }
    }

    private class TickReceiver extends BroadcastReceiver {
        public void attach() {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(TimerService.ACTION_START);
            filter.addAction(TimerService.ACTION_RESET);
            filter.addAction(TimerService.ACTION_TIMEOUT);
            filter.addAction(TimerService.ACTION_TICK);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(this, filter);
        }

        public void detach() {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            new TimerUpdater().refresh();
        }
    }
}
