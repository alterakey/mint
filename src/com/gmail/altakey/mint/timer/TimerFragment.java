package com.gmail.altakey.mint.timer;

import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

public class TimerFragment extends Fragment {
    public static final String TAG = "timer";

    private TickReceiver mTickReceiver = new TickReceiver();
    private CountDownTimer mUpdateTimer = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.timer, container, false);
        final View timer = v.findViewById(R.id.timer);
        timer.setOnClickListener(new TimerClickListener());
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        mTickReceiver.detach();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTickReceiver.attach();
        poke();
    }

    private void poke() {
        final Intent intent = new Intent(getActivity(), TimerService.class);
        intent.setAction(TimerService.ACTION_QUERY);
        getActivity().startService(intent);
    }

    private void toggle() {
        final Intent intent = new Intent(getActivity(), TimerService.class);
        intent.setAction(TimerService.ACTION_TOGGLE);
        getActivity().startService(intent);
    }

    private void stopUpdate() {
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
    }

    private void startUpdate(long remaining) {
        stopUpdate();
        mUpdateTimer = new CountDownTimer(remaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                final long seconds = millisUntilFinished / 1000 % 60;
                final long minutes = millisUntilFinished / 60000;

                final View root = getView();
                if (root != null) {
                    ((TextView)root.findViewById(R.id.min)).setText(String.format("%02d", minutes));
                    ((TextView)root.findViewById(R.id.sec)).setText(String.format("%02d", seconds));
                }
            }

            @Override
            public void onFinish() {
                onTick(0);
            }
        }.start();
    }

    private class TimerClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            toggle();
        }
    }

    private class TickReceiver extends BroadcastReceiver {
        public void attach() {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(TimerService.ACTION_QUERY);
            filter.addAction(TimerService.ACTION_TOGGLE);
            filter.addAction(TimerService.ACTION_TIMEOUT);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(this, filter);
        }

        public void detach() {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final int state = intent.getIntExtra(TimerService.EXTRA_STATE, TimerService.STATE_RESET);
            final long due = intent.getLongExtra(TimerService.EXTRA_DUE, 0);
            long remaining = 25 * 60 * 1000;

            if (due > 0) {
                remaining = due - SystemClock.elapsedRealtime();
            }

            if (state != TimerService.STATE_RESET) {
                startUpdate(remaining);
            } else {
                stopUpdate();
            }
        }
    }
}
