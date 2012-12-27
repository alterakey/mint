package com.gmail.altakey.mint;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.content.Intent;

import java.util.List;
import java.util.LinkedList;
import java.io.IOException;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;

import java.util.Date;
import java.util.Formatter;
import java.util.Queue;
import android.app.ProgressDialog;
import android.content.DialogInterface;

public class LoginFailedFragment extends Fragment {
    private static final int REQ_SET_LOGIN = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_failed, root, false);
        view.findViewById(R.id.tap_to_set_login).setOnClickListener(new SetLoginAction());
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_SET_LOGIN) {
            new CredentialTestTask().execute();
        }
    }

    public class CredentialTestTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog mmDialog;
        private boolean mmShouldResume = true;

        @Override
        protected void onPreExecute() {
            mmDialog = new ProgressDialog(getActivity());
            mmDialog.setTitle("Logging in");
            mmDialog.setMessage("Authenticating to Toodledo...");
            mmDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mmDialog.setIndeterminate(true);
            mmDialog.setCancelable(true);
            mmDialog.setCanceledOnTouchOutside(true);
            mmDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                    mmShouldResume = false;
                }
            });
            mmDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Authenticator.create(getActivity()).authenticate();
            } catch (Authenticator.FailureException e) {
                mmShouldResume = false;
            } catch (Authenticator.ErrorException e) {
            } catch (Authenticator.BogusException e) {
            } catch (IOException e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void ret) {
            mmDialog.dismiss();
            if (mmShouldResume) {
                getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag, new TaskListFragment(), TaskListFragment.TAG)
                    .commitAllowingStateLoss();
            }
        }
    }

    public class SetLoginAction implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivityForResult(new Intent(getActivity(), ConfigActivity.class), REQ_SET_LOGIN);
        }
    }
}
