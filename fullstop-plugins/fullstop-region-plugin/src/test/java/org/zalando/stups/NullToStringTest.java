package org.zalando.stups;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class NullToStringTest {

    @Test(expected = NullPointerException.class)
    public void test() {
        Object x = null;
        x.toString();
    }

    @Test
    public void listExpand() {
        List<String> testList = Lists.newArrayList("one", "two");
        System.out.println(testList);
    }
}
