package org.zalando.stups;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

public class NullToStringTest {

    @Test(expected = NullPointerException.class)
    public void test() {
        final Object x = null;
        x.toString();
    }

    @Test
    public void listExpand() {
        final List<String> testList = Lists.newArrayList("one", "two");
        System.out.println(testList);
    }
}
