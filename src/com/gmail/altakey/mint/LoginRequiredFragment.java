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

public class LoginRequiredFragment extends Fragment {
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
            getFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new TaskListFragment())
                .commitAllowingStateLoss();
        }
    }

    public class SetLoginAction implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivityForResult(new Intent(getActivity(), ConfigActivity.class), REQ_SET_LOGIN);
        }
    }
}
