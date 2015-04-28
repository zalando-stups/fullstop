package org.zalando.stups.fullstop.jobs;

import org.zalando.stups.fullstop.jobs.annotation.TenSeconds;

/**
 * @author  jbellmann
 */
public class SimpleScheduledBean {

    private int invocations;

    @TenSeconds
    public void run() {
        invocations++;
        System.out.println("CALLED");
    }

    public int getInvocationCount() {
        return invocations;
    }

}
