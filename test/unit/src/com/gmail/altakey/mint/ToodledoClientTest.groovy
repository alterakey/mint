package com.gmail.altakey.mint

import org.junit.runner.RunWith
import org.junit.Test
import org.junit.Before
import org.junit.After

import com.xtremelabs.robolectric.Robolectric
import com.xtremelabs.robolectric.tester.org.apache.http.TestHttpResponse;
import static org.mockito.Mockito.*;

import spock.lang.*

import android.app.Activity
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase

@RunWith(TestRunner)
class ToodledoClientTest extends Specification {
    @Test void test_000() {
        final def token = "abcdefg"
        def auth = when(mock(Authenticator).authenticate()).thenReturn(token).getMock()
        def corpse = new File("test/unit/fixtures/toodledo.folders.1.json").getBytes()

        Robolectric.addHttpResponseRule("GET", "http://api.toodledo.com/2/folders/get.php?key=${token}", new TestHttpResponse(200, corpse));

        def o = new ToodledoClient(auth, new Activity())

        when:
        def folders = o.getFolders()

        then:
        folders.collect() { it.id } == [6077681]
        folders.collect() { it.name } == ["2013カレンダー"]
    }

    @Test void test_001() {
        final def token = "abcdefg"
        def auth = when(mock(Authenticator).authenticate()).thenReturn(token).getMock()
        def corpse = new File("test/unit/fixtures/toodledo.tasks.1.json").getBytes()
        def corpse_folders = new File("test/unit/fixtures/toodledo.folders.json").getBytes()
        def corpse_contexts = new File("test/unit/fixtures/toodledo.contexts.json").getBytes()

        Robolectric.addHttpResponseRule("GET", "http://api.toodledo.com/2/tasks/get.php?key=${token}&modafter=0&fields=folder,context,star,priority", new TestHttpResponse(200, corpse));
        Robolectric.addHttpResponseRule("GET", "http://api.toodledo.com/2/folders/get.php?key=${token}", new TestHttpResponse(200, corpse_folders));
        Robolectric.addHttpResponseRule("GET", "http://api.toodledo.com/2/contexts/get.php?key=${token}", new TestHttpResponse(200, corpse_contexts));

        def o = new ToodledoClient(auth, new Activity())

        when:
        def tasks = o.getTasks()

        then:
        tasks.collect() { it.id } == [77838645]
        tasks.collect() { it.title } == ["florenceを掃除する"]
        tasks.collect() { it.folder } == [6032591]
        tasks.collect() { it.context } == [317141]
        tasks.collect() { it.star } == [0]
        tasks.collect() { it.priority } == [-1]
    }

    @Test void test_002() {
        final def token = "abcdefg"
        def auth = when(mock(Authenticator).authenticate()).thenReturn(token).getMock()
        def corpse = new File("test/unit/fixtures/toodledo.tasks.json").getBytes()
        def corpse_folders = new File("test/unit/fixtures/toodledo.folders.json").getBytes()
        def corpse_contexts = new File("test/unit/fixtures/toodledo.contexts.json").getBytes()
        def corpse_account = new File("test/unit/fixtures/toodledo.account.json").getBytes()

        Robolectric.addHttpResponseRule("GET", "http://api.toodledo.com/2/tasks/get.php?key=${token}&modafter=0&fields=folder,context,star,priority", new TestHttpResponse(200, corpse));
        Robolectric.addHttpResponseRule("GET", "http://api.toodledo.com/2/folders/get.php?key=${token}", new TestHttpResponse(200, corpse_folders));
        Robolectric.addHttpResponseRule("GET", "http://api.toodledo.com/2/contexts/get.php?key=${token}", new TestHttpResponse(200, corpse_contexts));
        Robolectric.addHttpResponseRule("GET", "http://api.toodledo.com/2/account/get.php?key=${token}", new TestHttpResponse(200, corpse_account));

        def context = new Activity()
        def o = new ToodledoClient(auth, context)
        def db = new ToodledoClient.DB(context).open()

        when:
        o.update()
        def cursor1 = db.rawQuery("select 1 from tasks", null)
        def cursor2 = db.rawQuery("select 1 from folders", null)
        def cursor3 = db.rawQuery("select 1 from contexts", null)

        then:
        [cursor1, cursor2, cursor3].collect() { it.getCount() } == [33, 24, 14]

        cleanup:
        [cursor1, cursor2, cursor3, db].each() { it == null || it.close() }
    }
}