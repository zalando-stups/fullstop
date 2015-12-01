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
package org.zalando.stups.fullstop.violation;


import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Objects;

public final class ViolationMatchers {

    private ViolationMatchers(){}

    public static Matcher<Violation> hasType(final String expectedType) {
        return new TypeSafeDiagnosingMatcher<Violation>() {
            @Override
            protected boolean matchesSafely(Violation violation, Description mismatchDescription) {
                final String actualType = violation.getViolationType();
                if (!Objects.equals(actualType, expectedType)) {
                    mismatchDescription.appendText("type was ").appendValue(actualType);
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("violation of type ").appendValue(expectedType);
            }
        };
    }
}
