package org.zalando.stups.fullstop.testing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;

/**
 * @author jbellmann
 */
@Profile("it-with-timeout")
@Component
public class AutoShutdown implements ApplicationContextAware {

    private final Logger LOG = LoggerFactory.getLogger(AutoShutdown.class);

    private final long startupTimestamp = System.currentTimeMillis();

    @Value("${fullstop.autoshutdown.timeout:2}")
    protected int timeout;

    private long timeOutInMilliseconds = 0;

    private AbstractApplicationContext aac = null;

    @PostConstruct
    public void init() {
        timeOutInMilliseconds = TimeUnit.MILLISECONDS.convert(timeout, TimeUnit.MINUTES);
    }

    @Scheduled(initialDelay = 10 * 1000, fixedDelay = 10 * 1000)
    public void shutdown() {
        LOG.warn("CHECK FOR AUTOSHUTDOWN, TIMEOUT WAS SET TO {} MIN", timeout);

        long diff = currentTimeMillis() - timeOutInMilliseconds;
        if (diff > startupTimestamp) {
            LOG.warn("CLOSE APPLICATION_CONTEX FOR AUTOSHUTDOWN");
            this.aac.close();
        }
        else {
            LOG.warn("CHECK FOR AUTOSHUTDOWN, DIFF : {} ms", startupTimestamp - diff);
        }
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.aac = (AbstractApplicationContext) applicationContext;
    }
}
