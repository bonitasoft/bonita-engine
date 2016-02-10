/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.matchers;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * @author Baptiste Mesta
 */
public class BonitaMatcher<T> extends BaseMatcher<T> {

    private final List<Matcher<T>> matchers;

    public BonitaMatcher(final Matcher<T> matcher) {
        matchers = new ArrayList<Matcher<T>>();
        matchers.add(matcher);
    }

    public static <T> BonitaMatcher<T> match(final Matcher<T> matcher) {
        return new BonitaMatcher<T>(matcher);
    }

    public BonitaMatcher<T> and(final Matcher<T> matcher) {
        matchers.add(matcher);
        return this;
    }

    @Override
    public boolean matches(final Object item) {
        boolean matche = true;
        for (final Matcher<T> matcher : matchers) {
            matche &= matcher.matches(item);
            if (!matche) {
                return false;
            }
        }
        return matche;
    }

    @Override
    public void describeTo(final Description description) {
        for (final Matcher<T> matcher : matchers) {
            matcher.describeTo(description);
        }
    }

}
