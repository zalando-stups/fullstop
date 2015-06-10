/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.SmartLifecycle;

import org.springframework.stereotype.Component;

import org.springframework.util.Assert;

import com.amazonaws.services.cloudtrail.processinglibrary.AWSCloudTrailProcessingExecutor;

/**
 * @author  jbellmann
 */
@Component
public class AwsCloudTrailProcessingExecutorContainer implements SmartLifecycle {

    private final AWSCloudTrailProcessingExecutor executor;

    @Autowired
    private FullstopContainerProperties fullstopContainerProperties = new FullstopContainerProperties();

    @Autowired
    public AwsCloudTrailProcessingExecutorContainer(final AWSCloudTrailProcessingExecutor executor) {
        this.executor = executor;
    }

    private volatile boolean running = false;

    @Override
    public synchronized void start() {
        if (isRunning()) {
            return;
        }

        Assert.notNull(executor, "Executor should never be null");
        executor.start();
        running = true;
    }

    @Override
    public synchronized void stop() {
        if (!isRunning()) {
            return;
        }

        if (executor != null) {
            executor.stop();
            running = false;
        }
    }

    @Override
    public boolean isRunning() {

        return running;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public boolean isAutoStartup() {

        return fullstopContainerProperties.isAutoStart();
    }

    @Override
    public void stop(final Runnable callback) {
        stop();
        callback.run();
    }

}
