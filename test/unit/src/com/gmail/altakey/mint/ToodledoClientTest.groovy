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
        def corpse = new File("test/unit/fixtures/toodledo.folders.1.json").getBytes()

        Robolectric.addHttpResponseRule("GET", "http://api.toodledo.com/2/folders/get.php?key=${token}", new TestHttpResponse(200, corpse));

        def o = new ToodledoClient(auth)

        when:
        def folders = o.getFolders()

        then:
        folders.collect() { it.id } == ["6077681"]
        folders.collect() { it.name } == ["2013カレンダー"]
    }

    @Test void test_001() {
        final def token = "abcdefg"
        def auth = when(mock(Authenticator).authenticate()).thenReturn(token).getMock()
        def corpse = new File("test/unit/fixtures/toodledo.tasks.1.json").getBytes()
        def corpse_folders = new File("test/unit/fixtures/toodledo.folders.json").getBytes()
        def corpse_contexts = new File("test/unit/fixtures/toodledo.contexts.json").getBytes()

        Robolectric.addHttpResponseRule("GET", "http://api.toodledo.com/2/tasks/get.php?key=${token}&fields=folder,context,star,priority", new TestHttpResponse(200, corpse));
        Robolectric.addHttpResponseRule("GET", "http://api.toodledo.com/2/folders/get.php?key=${token}", new TestHttpResponse(200, corpse_folders));
        Robolectric.addHttpResponseRule("GET", "http://api.toodledo.com/2/contexts/get.php?key=${token}", new TestHttpResponse(200, corpse_contexts));

        def o = new ToodledoClient(auth)

        when:
        def tasks = o.getTasks()

        then:
        tasks.collect() { it.id } == ["77838645"]
        tasks.collect() { it.title } == ["florenceを掃除する"]
        tasks.collect() { it.folder } == ["プライベート"]
        tasks.collect() { it.context } == ["家"]
        tasks.collect() { it.star } == ["0"]
        tasks.collect() { it.priority } == ["-1"]
    }
}