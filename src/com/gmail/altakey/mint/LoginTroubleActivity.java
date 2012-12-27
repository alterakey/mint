package com.gmail.altakey.mint;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.IOException;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashMap;

public class LoginTroubleActivity extends Activity
{
    public static final String KEY_TYPE = "type";
    public static final String TYPE_REQUIRED = "required";
    public static final String TYPE_FAILED = "failed";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plate);

        Intent intent = getIntent();
        String type = intent.getStringExtra(KEY_TYPE);

        if (TYPE_REQUIRED.equals(type)) {
            getFragmentManager()
                .beginTransaction()
                .add(R.id.frag, new LoginRequiredFragment())
                .commit();
        } else if (TYPE_FAILED.equals(type)) {
            getFragmentManager()
                .beginTransaction()
                .add(R.id.frag, new LoginFailedFragment())
                .commit();
        }
    }

    private void resumeToTaskList() {
        Intent intent = new Intent(this, TaskListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private class LoginRequiredFragment extends Fragment {
        private static final int REQ_SET_LOGIN = 1;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.login_required, root, false);
            view.findViewById(R.id.tap_to_set_login).setOnClickListener(new SetLoginAction());
            return view;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQ_SET_LOGIN) {
                if (!Authenticator.create(getActivity()).bogus()) {
                    resumeToTaskList();
                }
            }
        }

        private class SetLoginAction implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), ConfigActivity.class), REQ_SET_LOGIN);
            }
        }
    }

    private class LoginFailedFragment extends Fragment {
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
                    resumeToTaskList();
                }
            }
        }

        private class SetLoginAction implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), ConfigActivity.class), REQ_SET_LOGIN);
            }
        }
    }
}
