package com.gmail.altakey.mint.timer;

import android.app.IntentService;
import android.content.Intent;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class TimerService extends IntentService {
    public static final String ACTION_TOGGLE = "toggle";
    public static final String ACTION_QUERY = "query";
    public static final String ACTION_TIMEOUT = "timeout";
    public static final String EXTRA_DUE = "due";
    public static final String EXTRA_STATE = "state";

    public static final int STATE_RESET = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_BREAKING = 2;

    private int mState = STATE_RESET;
    private PendingIntent mDueIntent = null;
    private long mDueMillis = 0;

    public TimerService() {
        super("TimerService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        final String action = intent.getAction();
        Log.d("TS.oHI", String.format("state: %d, due: %d", mState, mDueMillis));
        if (ACTION_QUERY.equals(action)) {
            intent.putExtra(EXTRA_STATE, mState);
            intent.putExtra(EXTRA_DUE, mDueMillis);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else if (ACTION_TOGGLE.equals(action)) {
            toggle();
            intent.putExtra(EXTRA_STATE, mState);
            intent.putExtra(EXTRA_DUE, mDueMillis);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else if (ACTION_TIMEOUT.equals(action)) {
            proceed();
            intent.putExtra(EXTRA_STATE, mState);
            intent.putExtra(EXTRA_DUE, mDueMillis);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void startTimer(long intervalMillis, boolean for_break) {
        final AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        resetTimer();

        mDueMillis = SystemClock.elapsedRealtime() + intervalMillis;

        final Intent intent = new Intent(this, TimerService.class);
        intent.setAction(ACTION_TIMEOUT);
        mDueIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, mDueMillis, mDueIntent);
    }

    private void resetTimer() {
        final AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        if (mDueIntent != null) {
            am.cancel(mDueIntent);
            mDueIntent = null;
        }
        mDueMillis = 0;
    }

    private void toggle() {
        if (mState == STATE_RESET) {
            startTimer(25 * 60 * 1000, false);
            mState = STATE_RUNNING;
        } else {
            resetTimer();
            mState = STATE_RESET;
        }
    }

    private void proceed() {
        if (mState == STATE_RUNNING) {
            startTimer(5 * 60 * 1000, true);
            mState = STATE_BREAKING;
        } else {
            resetTimer();
            mState = STATE_RESET;
        }
    }
}
