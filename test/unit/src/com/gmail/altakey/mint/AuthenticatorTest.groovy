package com.gmail.altakey.mint

import org.junit.runner.RunWith
import org.junit.Test
import org.junit.Before
import org.junit.After

import com.xtremelabs.robolectric.Robolectric
import spock.lang.*

@RunWith(TestRunner)
class AuthenticatorTest extends Specification {
    @Test void test_000() {
        Robolectric.addPendingHttpResponse(200, "{\"token\":\"abcdefg\"}")

        when:
        def o = new Authenticator()
        def token = o.authenticate()

        then:
        token == "abcdefg"
    }

    @Test void test_001() {
        Robolectric.addPendingHttpResponse(200, "{}")

        when:
        def o = new Authenticator()
        def token = o.authenticate()

        then:
        token == null
    }
}