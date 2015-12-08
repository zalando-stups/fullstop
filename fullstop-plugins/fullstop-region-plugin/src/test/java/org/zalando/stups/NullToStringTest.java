package org.zalando.stups;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

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
