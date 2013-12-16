package com.gmail.altakey.mint;

import com.example.android.swipedismiss.*;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Arrays;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingActivity;

public class WelcomeActivity extends Activity
{
    public SyncPoker mSyncPokerManip = new SyncPoker(this);
    public LoginTroubleListener mTroubleListener = new LoginTroubleListener();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.plate);

        getFragmentManager()
            .beginTransaction()
            .add(R.id.frag, WelcomeFragment.newInstance(), TaskListFragment.TAG)
            .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTroubleListener.register();
        LoaderUtil.initLoader(0, null, mSyncPokerManip, getLoaderManager());
    }

    @Override
    public void onPause() {
        super.onPause();
        mTroubleListener.unregister();
    }

    private class LoginTroubleListener extends BroadcastReceiver {
        private Context getContext() {
            return WelcomeActivity.this;
        }

        public void register() {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(ToodledoClientService.ACTION_SYNC_DONE);
            filter.addAction(ToodledoClientService.ACTION_SYNC_ABORT);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(this, filter);
        }

        public void unregister() {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ToodledoClientService.ACTION_SYNC_DONE.equals(action)) {
                getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag, new TaskListFragment(), TaskListFragment.TAG)
                    .commit();
            } else if (ToodledoClientService.ACTION_SYNC_ABORT.equals(action)) {
                final String message = String.format("sync failed: %s", intent.getStringExtra(ToodledoClientService.EXTRA_ABORT_REASON));
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
