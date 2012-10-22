package com.gmail.altakey.mint;

import java.io.InputStreamReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.commons.codec.binary.Hex;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.SystemClock;

public class Authenticator {
    private static final String PREFERENCE_KEY = "auth_token";
    private static final String PREFERENCE_USER_ID = "auth_user_id";
    private static final String PREFERENCE_NOT_AFTER = "auth_not_after";
    private static final long TTL = 3 * 3600 * 1000;

    public static final String APP_NAME = "mint";
    public static final String APP_ID = "api4f508532c789a";

    private String mToken;
    private long mNotAfter;
    private Context mContext;
    private String mUserId;
    private String mEmail;
    private String mPassword;

    public Authenticator(Context c, String email, String password) {
        mContext = c;
        mEmail = email;
        mPassword = password;
    }

    public String authenticate() throws IOException, NoSuchAlgorithmException {
        Gson gson = new Gson();
        HttpClient client = new DefaultHttpClient();
        HttpGet req;
        HttpResponse response;
        HttpEntity entity;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);

        long now = System.currentTimeMillis();

        if (!invalid(now))
            return mToken;

        mToken = pref.getString(PREFERENCE_KEY, null);
        mNotAfter = pref.getLong(PREFERENCE_NOT_AFTER, 0);

        if (invalid(now)) {
            req = new HttpGet(
                String.format(
                    "http://api.toodledo.com/2/account/token.php?"
                    + "appid=%s&"
                    + "userid=%s&"
                    + "sig=%s",
                    APP_NAME,
                    lookup(),
                    getSignature()
                    )
                );
            response = client.execute(req);
            entity = response.getEntity();
            HashMap<String, String> tokenResponse = gson.fromJson(new InputStreamReader(entity.getContent()), new TypeToken<HashMap<String, String>>() {}.getType());
            entity.consumeContent();
            System.out.println(tokenResponse);
            mToken = tokenResponse.get("token");
            mNotAfter = now + TTL;
            pref.edit()
                .putString(PREFERENCE_KEY, mToken)
                .putLong(PREFERENCE_NOT_AFTER, mNotAfter)
                .commit();
        }
        return mToken;
    }

    private String lookup() throws IOException, NoSuchAlgorithmException {
        Gson gson = new Gson();
        HttpClient client = new DefaultHttpClient();
        HttpGet req;
        HttpResponse response;
        HttpEntity entity;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (mUserId != null)
            return mUserId;

        mUserId = pref.getString(PREFERENCE_USER_ID, null);

        if (mUserId == null) {
            req = new HttpGet(
                String.format(
                    "http://api.toodledo.com/2/account/lookup.php?"
                    + "appid=%s&"
                    + "email=%s&"
                    + "pass=%s&"
                    + "sig=%s",
                    APP_NAME,
                    mEmail,
                    mPassword,
                    getLookupSignature()
                    )
                );
            response = client.execute(req);
            entity = response.getEntity();
            HashMap<String, String> tokenResponse = gson.fromJson(new InputStreamReader(entity.getContent()), new TypeToken<HashMap<String, String>>() {}.getType());
            entity.consumeContent();
            mUserId = tokenResponse.get("userid");
            pref.edit()
                .putString(PREFERENCE_USER_ID, mUserId)
                .commit();
        }
        return mUserId;
    }

    public void revoke() {
        mToken = null;
        mNotAfter = 0;
        PreferenceManager.getDefaultSharedPreferences(mContext)
            .edit()
            .putString(PREFERENCE_KEY, null)
            .putLong(PREFERENCE_NOT_AFTER, 0)
            .commit();
    }

    public void unlink() {
        revoke();
        mUserId = null;
        PreferenceManager.getDefaultSharedPreferences(mContext)
            .edit()
            .putString(PREFERENCE_USER_ID, null)
            .commit();
    }

    private final String getSignature() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(mUserId.getBytes());
        md.update(APP_ID.getBytes());
        return Hex.encodeHexString(md.digest());
    }

    private final String getLookupSignature() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(mEmail.getBytes());
        md.update(APP_ID.getBytes());
        return Hex.encodeHexString(md.digest());
    }

    private boolean invalid(long at) {
        return (mToken == null || mNotAfter < at);
    }
}
