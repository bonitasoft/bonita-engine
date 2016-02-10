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

import org.bonitasoft.engine.bpm.NamedElement;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * @author Baptiste Mesta
 */
public class NameMatcher extends BaseMatcher<NamedElement> {

    private final String name;

    /**
     * @param name
     */
    public NameMatcher(final String name) {
        this.name = name;
    }

    public static NameMatcher nameIs(final String name) {
        return new NameMatcher(name);
    }

    @Override
    public boolean matches(final Object item) {
        return name.equals(((NamedElement) item).getName());
    }

    @Override
    public void describeTo(final Description description) {
    }
}
