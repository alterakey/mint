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

import android.util.Log;

public class TimerFragment extends Fragment {
    public static final String TAG = "timer";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_SECOND = "second";

    private TickReceiver mTickReceiver = new TickReceiver();
    private CountDownTimer mUpdateTimer = null;

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
    public void onPause() {
        super.onPause();
        mTickReceiver.detach();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTickReceiver.attach();
        new TimerUpdater().update();
    }

    private void toggle() {
        final Intent intent = new Intent(getActivity(), TimerService.class);
        intent.setAction(TimerService.ACTION_TOGGLE);
        getActivity().startService(intent);
    }

    private class TimerClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            toggle();
        }
    }

    private class TimerUpdater {
        public void update() {
            final int state = TimerService.getState();
            final long remaining = TimerService.getRemaining(TimerService.getDueMillis());

            refresh(remaining);

            if (state != TimerService.STATE_RESET) {
                activate(remaining);
            } else {
                deactivate();
            }
        }

        private void refresh(long remaining) {
            final long seconds = remaining / 1000 % 60;
            final long minutes = remaining / 60000;

            final View root = getView();
            if (root != null) {
                ((TextView)root.findViewById(R.id.min)).setText(String.format("%02d", minutes));
                ((TextView)root.findViewById(R.id.sec)).setText(String.format("%02d", seconds));
            }
        }

        private void deactivate() {
            if (mUpdateTimer != null) {
                mUpdateTimer.cancel();
                mUpdateTimer = null;
            }
        }

        private void activate(long remaining) {
            deactivate();
            mUpdateTimer = new CountDownTimer(remaining, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    refresh(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    onTick(0);
                }
            }.start();
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
            new TimerUpdater().update();
        }
    }
}
