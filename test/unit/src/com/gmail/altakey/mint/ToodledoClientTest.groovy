package com.gmail.altakey.mint

import org.junit.runner.RunWith
import org.junit.Test
import org.junit.Before
import org.junit.After

import com.xtremelabs.robolectric.Robolectric

import spock.lang.*

@RunWith(TestRunner)
class ToodledoClientTest extends Specification {
    @Test void test_000() {
        Authenticator auth = Mock()
        auth.authenticate() >>> "abcdefg"

        Robolectric.addPendingHttpResponse(200, "{\"a\", \"b\", \"c\"}")

        def o = new ToodledoClient(auth)

        when:
        byte[] bytes = o.getFolders()
        def message = new String(bytes, "UTF-8")

        then:
        message == "{\"a\", \"b\", \"c\"}"
        1 * auth.authenticate()
    }
}