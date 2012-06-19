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

public class Authenticator {
    public static final String APP_NAME = "mint";
    public static final String USER_ID = "";
    public static final String APP_ID = "api4f508532c789a";
    public static final String USER_PASSWORD = "";

    private String token;

    public void authenticate() throws IOException, NoSuchAlgorithmException {
        Gson gson = new Gson();
        HttpClient client = new DefaultHttpClient();
        HttpGet req;
        HttpResponse response;
        HttpEntity entity;

        req = new HttpGet(
            String.format(
                "http://api.toodledo.com/2/account/token.php?"
                + "appid=%s&"
                + "userid=%s&"
                + "sig=%s",
                APP_NAME,
                USER_ID,
                getSignature()
                )
            );
        response = client.execute(req);
        entity = response.getEntity();
        HashMap<String, String> tokenResponse = gson.fromJson(new InputStreamReader(entity.getContent()), new TypeToken<HashMap<String, String>>() {}.getType());
        entity.consumeContent();
        System.out.println(tokenResponse);
        this.token = tokenResponse.get("token");
    }

    public String getKey() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(USER_PASSWORD.getBytes());
        String hashed_password = Hex.encodeHexString(md.digest());

        md.reset();
        md.update(hashed_password.getBytes());
        md.update(APP_ID.getBytes());
        md.update(this.token.getBytes());
        return Hex.encodeHexString(md.digest());
    }

    private static final String getSignature() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(USER_ID.getBytes());
        md.update(APP_ID.getBytes());
        return Hex.encodeHexString(md.digest());
    }

}
