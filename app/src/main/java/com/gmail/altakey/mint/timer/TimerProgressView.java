package com.gmail.altakey.mint.timer;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TimerProgressView extends FrameLayout {
    private boolean mBreaking;

    public TimerProgressView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public TimerProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public TimerProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public TimerProgressView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context c, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        View.inflate(c, R.layout.view_timer_progress, this);
    }

    public void setBreaking(final boolean idle) {
        if (mBreaking != idle) {
            mBreaking = idle;
            if (mBreaking) {
                findViewById(R.id.idle).setVisibility(View.VISIBLE);
                findViewById(R.id.busy).setVisibility(View.GONE);
            } else {
                findViewById(R.id.busy).setVisibility(View.VISIBLE);
                findViewById(R.id.idle).setVisibility(View.GONE);
            }

            final ProgressBar progress = getCurrentProgressBar();
            progress.setMax(1);
            progress.setProgress(1);
        }
    }

    public void setProgress(final long remaining, final long duration) {
        final ProgressBar progress = getCurrentProgressBar();
        if (duration > 0) {
            progress.setMax((int) duration);
            progress.setProgress((int)remaining);
        } else {
            progress.setMax(1);
            progress.setProgress(1);
        }
    }

    private ProgressBar getCurrentProgressBar() {
        if (mBreaking) {
            return (ProgressBar)findViewById(R.id.idle);
        } else {
            return (ProgressBar)findViewById(R.id.busy);
        }
    }
}
