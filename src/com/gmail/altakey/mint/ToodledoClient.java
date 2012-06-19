package com.gmail.altakey.mint;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class ToodledoClient {
    public static void main(String args[]) throws IOException, NoSuchAlgorithmException {
        HttpClient client = new DefaultHttpClient();
        Authenticator authenticator = new Authenticator();

        authenticator.authenticate();

        HttpGet req = new HttpGet(
            String.format(
                "http://api.toodledo.com/2/folders/get.php?"
                + "key=%s",
                authenticator.getKey()
            )
        );
        HttpResponse response = client.execute(req);
        HttpEntity entity = response.getEntity();
        entity.writeTo(System.out);
        entity.consumeContent();
    }
}