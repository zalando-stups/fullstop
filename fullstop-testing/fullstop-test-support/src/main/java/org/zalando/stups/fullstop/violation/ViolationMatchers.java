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
            protected boolean matchesSafely(final Violation violation, final Description mismatchDescription) {
                final String actualType = violation.getViolationType();
                if (!Objects.equals(actualType, expectedType)) {
                    mismatchDescription.appendText("type was ").appendValue(actualType);
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("violation of type ").appendValue(expectedType);
            }
        };
    }
}
