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
package org.zalando.stups.fullstop.web.test;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;

import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author mrandi
 */
public final class MatcherHelper {

    public static <T> Matcher<? super Collection<T>> hasSize(final int expectedSize) {
        return new TypeSafeDiagnosingMatcher<Collection<?>>() {
            @Override
            protected boolean matchesSafely(final Collection<?> actual, final Description mismatchDescription) {
                final int actualSize = actual.size();
                if (actualSize == expectedSize) {
                    return true;
                }
                else {
                    mismatchDescription.appendText("size was ").appendValue(actualSize);
                    return false;
                }
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("with size ").appendValue(expectedSize);
            }
        };
    }

    public static Matcher<? super CharSequence> empty() {
        return new TypeSafeDiagnosingMatcher<CharSequence>() {
            @Override
            protected boolean matchesSafely(final CharSequence item, final Description mismatchDescription) {
                if (item.length() == 0) {
                    return true;
                }
                else {
                    mismatchDescription.appendText("was ").appendValue(item);
                    return false;
                }
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("empty string");
            }
        };
    }

    public static <T> TypeSafeMatcher<? extends T> meetsAssertions(final Assertable<? super T> assertions) {
        return new TypeSafeMatcher<T>() {
            private final Logger log = getLogger(getClass());

            @Override
            protected boolean matchesSafely(final T item) {
                try {
                    assertions.doAssertions(item);
                    return true;
                }
                catch (final AssertionError e) {
                    log.error("Assertions failed", e);
                    return false;
                }
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("object that meets all assertions");
            }
        };
    }

    public static interface Assertable<T> {

        void doAssertions(T item) throws AssertionError;

    }
}
