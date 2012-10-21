package com.gmail.altakey.mint

import org.junit.runner.RunWith
import org.junit.Test
import org.junit.Before
import org.junit.After

import com.xtremelabs.robolectric.Robolectric
import com.xtremelabs.robolectric.tester.org.apache.http.TestHttpResponse;
import static org.mockito.Mockito.*;

import spock.lang.*

@RunWith(TestRunner)
class ToodledoClientTest extends Specification {
    @Test void test_000() {
        final def token = "abcdefg"
        def auth = when(mock(Authenticator).authenticate()).thenReturn(token).getMock()

        Robolectric.addHttpResponseRule("GET", "http://api.toodledo.com/2/folders/get.php?key=${token}", new TestHttpResponse(200, "{\"a\", \"b\", \"c\"}"));

        def o = new ToodledoClient(auth)

        when:
        byte[] bytes = o.getFolders()
        def message = new String(bytes, "UTF-8")

        then:
        message == "{\"a\", \"b\", \"c\"}"
    }
}