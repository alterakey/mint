package com.gmail.altakey.mint;

import android.test.AndroidTestCase;
import android.util.Log;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;


public class AuthenticatorTest extends AndroidTestCase {

    public void test_001() throws IOException, NoSuchAlgorithmException {
        new Authenticator().authenticate();
    }

}
