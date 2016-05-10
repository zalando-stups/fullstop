package org.zalando.stups.fullstop;

import com.amazonaws.services.cloudtrail.processinglibrary.interfaces.ProgressReporter;
import com.amazonaws.services.cloudtrail.processinglibrary.progress.ProgressStatus;

/**
 * Created by mrandi.
 */
public class NoOpsProgressReporter implements ProgressReporter {

    @Override
    public Object reportStart(final ProgressStatus status) {
        return null;
    }

    @Override
    public void reportEnd(final ProgressStatus status, final Object object) {
    }
}