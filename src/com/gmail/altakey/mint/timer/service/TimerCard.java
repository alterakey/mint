package com.gmail.altakey.mint.timer.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import com.gmail.altakey.mint.timer.R;
import com.gmail.altakey.mint.timer.util.TimerReader;
import com.google.android.glass.app.VoiceTriggers;
import com.google.android.glass.timeline.LiveCard;

import java.util.Timer;
import java.util.TimerTask;

public class TimerCard extends LiveCard {
    private RemoteViews mViews;

    public TimerCard(Context context, String tag) {
        super(context, tag);
        init(context);
    }

    private void init(final Context context) {
        mViews = new RemoteViews(context.getPackageName(), R.layout.card_timer);
        setViews(mViews);
    }

    public void setRemaining(final long remainingMillis) {
        final TimerReader reader = new TimerReader(remainingMillis);
        mViews.setTextViewText(R.id.min, String.format("%02d", reader.minutes));
        mViews.setTextViewText(R.id.sec, String.format("%02d", reader.seconds));
        setViews(mViews);
    }
}
