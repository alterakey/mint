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

public class TaskListActivity extends SlidingActivity
{
    public static final String KEY_LIST_FILTER = "filter";

    public LoginTroubleListener mTroubleListener = new LoginTroubleListener();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.plate);
        setBehindContentView(R.layout.list_menu);

        final Intent intent = getIntent();
        FilterType filter = (FilterType)intent.getParcelableExtra(KEY_LIST_FILTER);
        if (filter == null) {
            filter = new FilterType().makeHot();
        }

        // configure the SlidingMenu
        final SlidingMenu menu = getSlidingMenu();
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);

        getFragmentManager()
            .beginTransaction()
            .add(R.id.frag, TaskListFragment.newInstance(filter), TaskListFragment.TAG)
            .commit();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setSlidingActionBarEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            toggle();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTroubleListener.register();
    }

    @Override
    public void onPause() {
        super.onPause();
        mTroubleListener.unregister();
    }

    private class LoginTroubleListener extends BroadcastReceiver {
        private Context getContext() {
            return TaskListActivity.this;
        }

        public void register() {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(ToodledoClientService.ACTION_LOGIN_REQUIRED);
            filter.addAction(ToodledoClientService.ACTION_SYNC_ABORT);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(this, filter);
        }

        public void unregister() {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ToodledoClientService.ACTION_LOGIN_REQUIRED.equals(action)) {
                final Intent i = new Intent(getContext(), WelcomeActivity.class);
                startActivity(i);
                finish();
            } else if (ToodledoClientService.ACTION_SYNC_ABORT.equals(action)) {
                final String message = String.format("sync failed: %s", intent.getStringExtra(ToodledoClientService.EXTRA_ABORT_REASON));
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
