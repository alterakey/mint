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
    @Test void test_000() {
        Robolectric.addPendingHttpResponse(200, "{\"token\":\"abcdefg\"}")

        when:
        def o = new Authenticator(new Activity())
        def token = o.authenticate()

        then:
        token == "abcdefg"
    }

    @Test void test_001() {
        Robolectric.addPendingHttpResponse(200, "{}")

        when:
        def o = new Authenticator(new Activity())
        def token = o.authenticate()

        then:
        token == null
    }

    @Test void test_002() {
        Robolectric.addPendingHttpResponse(200, "{\"token\":\"abcdefg\"}")

        when:
        def o = new Authenticator(new Activity())
        o.authenticate()
        def token = PreferenceManager.getDefaultSharedPreferences(Robolectric.application).getString(Authenticator.PREFERENCE_KEY, null)

        then:
        token == "abcdefg"
    }

}