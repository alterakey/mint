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
        def o = new Authenticator()

        when:
        o.authenticate()

        then:
        o.token == "abcdefg"
    }

    @Test void test_001() {
        Robolectric.addPendingHttpResponse(200, "{}")
        def o = new Authenticator()

        when:
        o.authenticate()

        then:
        o.token == null
    }
}