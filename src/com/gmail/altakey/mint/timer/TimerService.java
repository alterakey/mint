package com.gmail.altakey.mint.timer;

import android.app.Service;
import android.os.IBinder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.content.Intent;

import android.app.PendingIntent;
import android.app.AlarmManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Timer;
import java.util.TimerTask;
import android.media.SoundPool;
import android.media.AudioManager;
import android.util.Log;

public class TimerService extends Service {
    public static final String ACTION_START = "start";
    public static final String ACTION_RESET = "reset";
    public static final String ACTION_TIMEOUT = "timeout";
    public static final String ACTION_TICK = "tick";
    public static final String EXTRA_DUE = "due";
    public static final String EXTRA_STATE = "state";

    public static final int STATE_RESET = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_BREAKING = 2;

    private static int sState = STATE_RESET;
    private static long sDueMillis = 0;
    private PendingIntent mDueIntent = null;

    private Timer mTimer = null;
    private Ticker mTicker = new Ticker();

    private Looper mLooper;
    private ServiceHandler mHandler;

    private class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            final Intent intent = (Intent)msg.obj;
            handleIntent(intent);
        }
    }

    public static int getState() {
        return sState;
    }

    public static long getDueMillis() {
        return sDueMillis;
    }

    public static long getRemaining(long due) {
        if (due > 0) {
            return getDueMillis() - SystemClock.elapsedRealtime();
        } else {
            return 25 * 60 * 1000;
        }
    }

    @Override
    public void onCreate() {
        final HandlerThread thread = new HandlerThread("worker", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mLooper = thread.getLooper();
        mHandler = new ServiceHandler(mLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Message msg = mHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mHandler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void handleIntent(Intent intent) {
        final String action = intent.getAction();
        Log.d("TS.oHI", String.format("state: %d, due: %d", sState, sDueMillis));

        wakeUp();

        if (ACTION_START.equals(action)) {
            start();
            intent.putExtra(EXTRA_STATE, sState);
            intent.putExtra(EXTRA_DUE, sDueMillis);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else if (ACTION_RESET.equals(action)) {
            reset();
            intent.putExtra(EXTRA_STATE, sState);
            intent.putExtra(EXTRA_DUE, sDueMillis);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else if (ACTION_TIMEOUT.equals(action)) {
            proceed();
            intent.putExtra(EXTRA_STATE, sState);
            intent.putExtra(EXTRA_DUE, sDueMillis);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

        if (sState == STATE_RESET) {
            stopSelf();
        }
    }

    private void wakeUp() {
        final PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE | PowerManager.ACQUIRE_CAUSES_WAKEUP, "screen_poke");
        try {
            wl.acquire();
        } finally {
            wl.release();
        }
    }

    private void startTimer(long intervalMillis, boolean for_break) {
        final AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        resetTimer();

        sDueMillis = SystemClock.elapsedRealtime() + intervalMillis;

        final Intent intent = new Intent(this, TimerService.class);
        intent.setAction(ACTION_TIMEOUT);
        mDueIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, sDueMillis, mDueIntent);

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                LocalBroadcastManager.getInstance(TimerService.this).sendBroadcast(new Intent(TimerService.ACTION_TICK));
            }
        }, 0, 1000);

        mTicker.prepare();
    }

    private void resetTimer() {
        final AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        if (mDueIntent != null) {
            am.cancel(mDueIntent);
            mDueIntent = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        sDueMillis = 0;
        mTicker.cleanup();
    }

    private void start() {
        if (sState == STATE_RESET) {
            startTimer(25 * 60 * 1000, false);
            sState = STATE_RUNNING;
        }
    }

    private void reset() {
        if (sState != STATE_RESET) {
            resetTimer();
            sState = STATE_RESET;
        }
    }

    private void proceed() {
        if (sState == STATE_RUNNING) {
            startTimer(5 * 60 * 1000, true);
            sState = STATE_BREAKING;
        } else {
            resetTimer();
            sState = STATE_RESET;
        }
    }


    private class Ticker {
        private SoundPool mmPool = null;
        private int mmSoundTick = 0;
        private int mmSoundBell = 0;

        public void prepare() {
            mmPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
            mmSoundTick = mmPool.load(TimerService.this, R.raw.tick, 1);
            mmSoundBell = mmPool.load(TimerService.this, R.raw.ring, 1);
        }

        public void cleanup() {
            mmSoundTick = 0;
            mmSoundBell = 0;
            if (mmPool != null) {
                mmPool.release();
                mmPool = null;
            }
        }

        public void tick() {
            if (mmPool != null) {
                mmPool.play(mmSoundTick, 1.0f, 1.0f, 0, 0, 1.0f);
            }
        }

        public void bell() {
            if (mmPool != null) {
                mmPool.play(mmSoundBell, 1.0f, 1.0f, 0, 1, 1.0f);
            }
        }
    }
}
