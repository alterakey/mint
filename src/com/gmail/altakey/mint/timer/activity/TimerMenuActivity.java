package com.gmail.altakey.mint.timer.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.gmail.altakey.mint.timer.R;
import com.gmail.altakey.mint.timer.service.TimerService;

public class TimerMenuActivity extends Activity {
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset_menu_item:
                final Intent intent = new Intent(this, TimerService.class);
                intent.setAction(TimerService.ACTION_RESET);
                startService(intent);
                break;
            case R.id.exit_menu_item:
                stopService(new Intent(this, TimerService.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        finish();
    }
}
