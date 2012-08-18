package com.gmail.altakey.mint;

import android.support.v4.app.FragmentActivity

import org.junit.runner.RunWith
import org.junit.Test
import org.junit.Before
import org.junit.After

import com.xtremelabs.robolectric.Robolectric
import spock.lang.*

@RunWith(TestRunner)
class TaskListFragmentTest extends Specification {
    @Test void test_000() {
        when:
        def o = new TaskListFragment()
        Robolectric.shadowOf(o).setActivity(new FragmentActivity())
        o.onCreate(null)

        then:
        true
    }

    @Test void test_001() {
        def o = new TaskListFragment()
        Robolectric.shadowOf(o).setActivity(new FragmentActivity())
        o.onCreate(null)

        when:
        def data = Robolectric.shadowOf(o).getListAdapter()

        then:
        4 == data.getCount()
    }
}