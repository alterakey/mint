package com.gmail.altakey.mint;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.util.List;
import java.io.UnsupportedEncodingException;

public class ToodledoClient {
    private Authenticator mAuth;

    public ToodledoClient(Authenticator auth) {
        mAuth = auth;
    }

    public List<Folder> getFolders() throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        HttpClient client = new DefaultHttpClient();

        HttpGet req = new HttpGet(
            String.format(
                "http://api.toodledo.com/2/folders/get.php?"
                + "key=%s",
                mAuth.authenticate()
            )
        );
        HttpResponse response = client.execute(req);
        HttpEntity entity = response.getEntity();
        entity.writeTo(os);
        entity.consumeContent();

        try {
            return new Gson().fromJson(os.toString("UTF-8"), List.class);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
