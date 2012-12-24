package com.gmail.altakey.mint;

import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;

public abstract class NetworkTask extends AsyncTask<Void, Void, Integer> {
    protected Exception mError;

    protected static final int OK = 0;
    protected static final int LOGIN_REQUIRED = 1;
    protected static final int LOGIN_FAILED = 2;
    protected static final int FAILURE = 3;

    @Override
    protected Integer doInBackground(Void... params) {
        try {
            doTask();
            return OK;
        } catch (IOException e) {
            mError = e;
            return FAILURE;
        } catch (Authenticator.BogusException e) {
            mError = e;
            return LOGIN_REQUIRED;
        } catch (Authenticator.FailureException e) {
            mError = e;
            return LOGIN_FAILED;
        } catch (Authenticator.Exception e) {
            mError = e;
            return FAILURE;
        }
    }

    abstract protected void doTask() throws IOException, Authenticator.Exception;

    @Override
    protected void onPostExecute(Integer ret) {
        if (ret == LOGIN_REQUIRED) {
            onLoginRequired();
        } else if (ret == LOGIN_FAILED) {
            onLoginFailed();
        } else if (ret == FAILURE) {
            onLoginError();
        }
    }

    protected void onLoginRequired() {
    }

    protected void onLoginFailed() {
    }

    protected void onLoginError() {
        Log.e("NT", "fetch failure", mError);
    }
}
