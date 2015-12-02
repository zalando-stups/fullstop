package org.zalando.stups.fullstop.web.test;

/**
 * @author ahartmann
 */
public class TestDataInitializer {

    public static final TestDataInitializer INITIALIZER = new TestDataInitializer();

    public <T> T create(final TestObjectBuilder<T> entityBuilder) {
        return entityBuilder.build();
    }

}
