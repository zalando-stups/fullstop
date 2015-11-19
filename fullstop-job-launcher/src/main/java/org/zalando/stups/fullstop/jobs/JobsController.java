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
package org.zalando.stups.fullstop.jobs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;

import static com.google.common.collect.Sets.newTreeSet;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/api/jobs", produces = APPLICATION_JSON_VALUE)
public class JobsController {

    private final Map<String, FullstopJob> jobBeans;

    private final ExecutorService executor = newSingleThreadExecutor();

    @Autowired
    public JobsController(Map<String, FullstopJob> jobBeans) {
        this.jobBeans = jobBeans;
    }

    @RequestMapping(method = GET)
    public SortedSet<String> getJobs() {
        return newTreeSet(jobBeans.keySet());
    }

    @RequestMapping(value = "/{name}/run", method = POST)
    public ResponseEntity<Void> runJob(@PathVariable String name) {
        final FullstopJob job = jobBeans.get(name);
        if (job == null) {
            return new ResponseEntity<>(NOT_FOUND);
        } else {
            executor.submit(job);
            return new ResponseEntity<>(ACCEPTED);
        }
    }

}
