package com.gmail.altakey.mint;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.widget.Toast;

import java.util.Set;
import java.util.HashSet;

public class Notifier {
    public static final String NOTIFY_LOGIN_REQUIRED = "login_required";
    public static final String NOTIFY_LOGIN_FAILED = "login_failed";

    private static final String KEY_DONE = "notify_done";

    private Context mContext;

    public Notifier(Context c) {
        mContext = c;
    }

    public void notify(String tickerText, String key) {
        final NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification notification = new Notification(R.drawable.icon, tickerText, System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
		final PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, ConfigActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
		notification.setLatestEventInfo(
				mContext,
				mContext.getText(R.string.app_name),
				tickerText,
				contentIntent);
		nm.notify(1, notification);
    }

    public void notifyOnce(String tickerText, String key) {
        if (!isMarked(key)) {
            notify(tickerText, key);
            mark(key);
        }
    }

    public void cancel() {
        final NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(1);
    }

    public void boo(String text) {
        Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
    }

    public void info(String text) {
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }

    public void clear() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        pref.edit()
            .remove(KEY_DONE)
            .commit();
    }

    public void unmark(String key) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Set<String> done = pref.getStringSet(KEY_DONE, new HashSet<String>());
        done.remove(key);
        pref.edit()
            .putStringSet(KEY_DONE, done)
            .commit();
    }

    public void mark(String key) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Set<String> done = pref.getStringSet(KEY_DONE, new HashSet<String>());
        done.add(key);
        pref.edit()
            .putStringSet(KEY_DONE, done)
            .commit();
    }

    public boolean isMarked(String key) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return pref.getStringSet(KEY_DONE, new HashSet<String>()).contains(key);
    }
}
