package com.gmail.altakey.mint.timer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        getFragmentManager()
            .beginTransaction()
            .add(R.id.frag, new TimerFragment(), TimerFragment.TAG)
            .commit();
    }
}
