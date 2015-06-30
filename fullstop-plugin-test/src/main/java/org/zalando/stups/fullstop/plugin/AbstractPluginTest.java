package org.zalando.stups.fullstop.plugin;

import org.junit.runner.RunWith;

import org.springframework.boot.test.SpringApplicationConfiguration;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.unknown.pkg.ExampleApplication;

/**
 * @author  jbellmann
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ExampleApplication.class)
public abstract class AbstractPluginTest { }
