package com.gmail.altakey.mint

import org.junit.runner.RunWith
import org.junit.Test
import org.junit.Before
import org.junit.After

import com.xtremelabs.robolectric.Robolectric
import spock.lang.*

import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.app.Activity

@RunWith(TestRunner)
class AuthenticatorTest extends Specification {
    private static final EMAIL = "test@example.org";
    private static final PASSWORD = "test";

    @Test void test_000() {
        Robolectric.addPendingHttpResponse(200, "{\"userid\":\"test\"}")
        Robolectric.addPendingHttpResponse(200, "{\"token\":\"abcdefg\"}")

        when:
        def o = new Authenticator(new Activity(), EMAIL, PASSWORD)
        def token = o.authenticate()

        then:
        token == "abcdefg"
    }

    @Test void test_001() {
        Robolectric.addPendingHttpResponse(200, "{\"userid\":\"test\"}")
        Robolectric.addPendingHttpResponse(200, "{}")

        when:
        def o = new Authenticator(new Activity(), EMAIL, PASSWORD)
        def token = o.authenticate()

        then:
        token == null
    }

    @Test void test_002() {
        Robolectric.addPendingHttpResponse(200, "{\"userid\":\"test\"}")
        Robolectric.addPendingHttpResponse(200, "{\"token\":\"abcdefg\"}")

        when:
        def o = new Authenticator(new Activity(), EMAIL, PASSWORD)
        o.authenticate()
        def token = PreferenceManager.getDefaultSharedPreferences(Robolectric.application).getString(Authenticator.PREFERENCE_KEY, null)

        then:
        token == "abcdefg"
    }

    @Test void test_003() {
        Robolectric.addPendingHttpResponse(200, "{\"userid\":\"test\"}")
        Robolectric.addPendingHttpResponse(200, "{\"token\":\"abcdefg\"}")

        when:
        def o = new Authenticator(new Activity(), EMAIL, PASSWORD)
        o.authenticate()
        o.revoke()
        def token = PreferenceManager.getDefaultSharedPreferences(Robolectric.application).getString(Authenticator.PREFERENCE_KEY, null)

        then:
        token == null
    }

    @Test void test_004() {
        Robolectric.addPendingHttpResponse(200, "{\"userid\":\"test\"}")
        Robolectric.addPendingHttpResponse(200, "{\"token\":\"abcdefg\"}")

        when:
        def o = new Authenticator(new Activity(), EMAIL, PASSWORD)
        o.authenticate()
        o.unlink()
        def token = PreferenceManager.getDefaultSharedPreferences(Robolectric.application).getString(Authenticator.PREFERENCE_USER_ID, null)

        then:
        token == null
    }
}