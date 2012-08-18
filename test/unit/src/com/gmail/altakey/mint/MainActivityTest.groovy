package com.gmail.altakey.mint;

import org.junit.runner.RunWith
import org.junit.Test
import org.junit.Before
import org.junit.After

import com.xtremelabs.robolectric.Robolectric
import spock.lang.*

@RunWith(TestRunner)
class MainActivityTest {
    @Test void test_000() {
        def o = new MainActivity()
        o.onCreate(null)
        assert true
    }
}