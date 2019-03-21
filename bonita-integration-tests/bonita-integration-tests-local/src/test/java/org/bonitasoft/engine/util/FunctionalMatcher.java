package org.bonitasoft.engine.util;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

public interface FunctionalMatcher<T> extends Matcher {

    boolean isMatchting(T item);

    @Override
    default boolean matches(Object item) {
        return isMatchting(((T) item));
    }

    @Override
    default void describeMismatch(Object item, Description mismatchDescription) {
        mismatchDescription.appendText("was ").appendValue(item);
    }

    @Override
    default void _dont_implement_Matcher___instead_extend_BaseMatcher_() {

    }

    @Override
    default void describeTo(Description description) {

    }
}
