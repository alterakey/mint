package com.gmail.altakey.mint.timer;

import android.app.IntentService;
import android.app.Service;
import android.os.IBinder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.content.Intent;
import android.content.Context;

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

import android.app.Notification;
import android.app.NotificationManager;
import android.widget.RemoteViews;

import com.google.android.glass.app.VoiceTriggers;
import com.google.android.glass.timeline.LiveCard;

public class TimerService extends Service {
    public static final String ACTION_START = "start";
    public static final String ACTION_RESET = "reset";
    public static final String ACTION_TIMEOUT = "timeout";
    public static final String ACTION_TICK = "tick";

    public static final int STATE_RESET = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_BREAKING = 2;

    private static int sState = STATE_RESET;
    private static long sDueMillis = 0;
    private PendingIntent mDueIntent = null;

    private Timer mTimer = null;
    private Timer mIdleTimer = null;
    private Ticker mTicker = new Ticker(this);

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

    private static String TAG = "com.gmail.altakey.mint.timer.main";
    private LiveCard mLiveCard;
    private RemoteViews mViews;

    @Override
    public void onCreate() {
        super.onCreate();
        mViews = new RemoteViews(getPackageName(), R.layout.card_timer);
        mLiveCard = new LiveCard(this, TAG);
        mLiveCard.attach(this);
        mLiveCard.setViews(mViews);
        mLiveCard.setAction(PendingIntent.getService(this, 1, new Intent(ACTION_START), 0));
        mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        Log.d("TS", "created");
    }

    @Override
    public void onDestroy() {
        Log.d("TS", "destroying", new Exception());
        mLiveCard.unpublish();
        mLiveCard = null;

        //mTicker.cleanup();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();
        Log.d("TS.oHI", String.format("state: %d, due: %d", sState, sDueMillis));

        updateView();
        wakeUp();

        if (ACTION_START.equals(action)) {
            start();
        } else if (ACTION_RESET.equals(action)) {
            reset();
        } else if (ACTION_TIMEOUT.equals(action)) {
            proceed();
        } else if (VoiceTriggers.ACTION_VOICE_TRIGGER.equals(action)) {
            start();
        }

        if (sState == STATE_RESET) {
            if (mIdleTimer == null) {
                mIdleTimer = new Timer();
                mIdleTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        stopSelf();
                    }
                }, 90000);
            }
        } else {
            if (mIdleTimer != null) {
                mIdleTimer.cancel();
                mIdleTimer.purge();
                mIdleTimer = null;
            }
        }
        return START_STICKY;
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
                updateView();
                //mTicker.tick();
            }
        }, 1000, 1000);
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
        updateView();
    }

    private void updateView() {
        final TimerReader reader = new TimerReader(getRemaining(getDueMillis()));
        mViews.setTextViewText(R.id.min, String.valueOf(reader.minutes));
        mViews.setTextViewText(R.id.sec, String.valueOf(reader.seconds));
        mLiveCard.setViews(mViews);
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
        mTicker.bell();
        if (sState == STATE_RUNNING) {
            startTimer(5 * 60 * 1000, true);
            sState = STATE_BREAKING;
        } else {
            resetTimer();
            sState = STATE_RESET;
        }
    }

    private static class Ticker {
        private final Context mContext;
        private SoundPool mPool = null;
        private int mSoundTick = 0;
        private int mSoundBell = 0;

        public Ticker(final Context c) {
            mContext = c;
        }

        public void prepare() {
            if (mPool == null) {
                mPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
                mSoundTick = mPool.load(mContext, R.raw.tick, 1);
                mSoundBell = mPool.load(mContext, R.raw.ring, 1);
            }
        }

        public void cleanup() {
            if (mPool != null) {
                mSoundTick = 0;
                mSoundBell = 0;
                mPool.release();
                mPool = null;
            }
        }

        public void tick() {
            if (mPool != null) {
                mPool.play(mSoundTick, 1.0f, 1.0f, 0, 0, 1.0f);
            }
        }

        public void bell() {
            if (mPool != null) {
                mPool.play(mSoundBell, 1.0f, 1.0f, 0, 1, 1.0f);
            }
        }
    }
}
