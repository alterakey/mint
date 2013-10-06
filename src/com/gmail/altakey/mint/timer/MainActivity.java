package com.gmail.altakey.mint.timer;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getFragmentManager()
            .beginTransaction()
            .add(R.id.frag, new TimerFragment(), TimerFragment.TAG)
            .commit();
    }
}
