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
    public JobsController(final Map<String, FullstopJob> jobBeans) {
        this.jobBeans = jobBeans;
    }

    @RequestMapping(method = GET)
    public SortedSet<String> getJobs() {
        return newTreeSet(jobBeans.keySet());
    }

    @RequestMapping(value = "/{name}/run", method = POST)
    public ResponseEntity<Void> runJob(@PathVariable final String name) {
        final FullstopJob job = jobBeans.get(name);
        if (job == null) {
            return new ResponseEntity<>(NOT_FOUND);
        } else {
            executor.submit(job);
            return new ResponseEntity<>(ACCEPTED);
        }
    }

}
