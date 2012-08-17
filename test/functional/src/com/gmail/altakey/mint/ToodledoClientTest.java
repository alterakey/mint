package com.gmail.altakey.mint;

import android.test.AndroidTestCase;
import android.util.Log;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;


public class ToodledoClientTest extends AndroidTestCase {

    public void test_001() throws IOException, NoSuchAlgorithmException {
        Log.d("TCT.test_001", new String(new ToodledoClient().getFolders(), "UTF-8"));
    }

}
