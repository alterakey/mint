package com.gmail.altakey.mint

import org.junit.runner.RunWith
import org.junit.Test
import org.junit.Before
import org.junit.After

import com.xtremelabs.robolectric.Robolectric

import spock.lang.*

@RunWith(TestRunner)
class ToodledoClientTest {
    @Test void test_000() {
        Robolectric.addPendingHttpResponse(200, "{\"token\":\"abcdefg\"}")
        Robolectric.addPendingHttpResponse(200, "{\"a\", \"b\", \"c\"}")
        def o = new ToodledoClient()

        when:
        byte[] bytes = o.getFolders()
        def message = new String(bytes, "UTF-8")

        then:
        message == "{\"a\", \"b\", \"c\"}"
    }
}