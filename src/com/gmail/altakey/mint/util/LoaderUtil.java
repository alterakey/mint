package com.gmail.altakey.mint.util;

import android.os.Bundle;
import android.app.LoaderManager;
import android.content.Loader;

public class LoaderUtil {
    public static <T> void initLoader(final int loaderId, final Bundle args, final LoaderManager.LoaderCallbacks<T> callbacks, final LoaderManager loaderManager) {
        final Loader<T> loader = loaderManager.getLoader(loaderId);
        if (loader != null && loader.isReset()) {
            loaderManager.restartLoader(loaderId, args, callbacks);
        } else {
            loaderManager.initLoader(loaderId, args, callbacks);
        }
    }
}
