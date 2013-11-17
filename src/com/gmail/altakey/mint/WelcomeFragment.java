package com.gmail.altakey.mint;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WelcomeFragment extends Fragment {
    public static WelcomeFragment newInstance() {
        return new WelcomeFragment();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.welcome, root, false);
        view.findViewById(R.id.tap_to_set_login).setOnClickListener(new SetLoginAction());
        return view;
    }

    public class SetLoginAction implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(getActivity(), ConfigActivity.class));
        }
    }
}
