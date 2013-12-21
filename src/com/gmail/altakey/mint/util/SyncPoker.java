package com.gmail.altakey.mint.util;

import android.app.LoaderManager;
import android.app.Fragment;
import android.content.Context;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import com.gmail.altakey.mint.util.Authenticator;
import com.gmail.altakey.mint.service.ToodledoClient;
import com.gmail.altakey.mint.service.ToodledoClientService;

public class SyncPoker implements LoaderManager.LoaderCallbacks<Void> {
    private Context mContext;
    private Fragment mFragment;

    public SyncPoker(final Context c) {
        mContext = c;
    }

    public SyncPoker(final Fragment f) {
        mFragment = f;
    }

    private Context getContext() {
        if (mFragment != null) {
            return mFragment.getActivity();
        } else {
            return mContext;
        }
    }
    
    @Override
    public Loader<Void> onCreateLoader(final int id, final Bundle args) {
        return new AsyncTaskLoader<Void>(getContext()) {
            @Override
            public Void loadInBackground() {
                try {
                    final Context me = getContext();
                    final ToodledoClient client = new ToodledoClient(Authenticator.create(me), me);
                    final ToodledoClientService.Synchronizer sync = new ToodledoClientService.Synchronizer(me, client);
                    sync.update();
                    return null;
                } catch (final IOException e) {
                    Log.d("MA.SP", "ignoring", e);
                    return null;
                } catch (final Authenticator.BogusException e) {
                    Log.d("MA.SP", "ignoring", e);
                    return null;
                } catch (final Authenticator.FailureException e) {
                    Log.d("MA.SP", "ignoring", e);
                    return null;
                } catch (final Authenticator.ErrorException e) {
                    Log.d("MA.SP", "ignoring", e);
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(final Loader<Void> loader, final Void data) {
    }

    @Override
    public void onLoaderReset(final Loader<Void> loader) {
    }
}
