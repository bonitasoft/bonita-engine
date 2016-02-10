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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * @author Emmanuel Duchastenier
 */
public class ListContainsMatcher extends BaseMatcher<List<?>> {

    private final List<String> names;

    public ListContainsMatcher(final String[] names) {
        this.names = Arrays.asList(names);

    }

    public static ListContainsMatcher namesContain(final String... names) {
        return new ListContainsMatcher(names);
    }

    @Override
    public boolean matches(final Object item) {
        final List<?> list = (List<?>) item;
        try {
            if (names.size() != list.size()) {
                return false;
            }
            final Method m = list.get(0).getClass().getMethod("getName");
            List<String> lists = new ArrayList<String>();
            for (int i = 0; i < names.size(); i++) {
                final String invoke = (String) m.invoke(list.get(i), null);
                lists.add(invoke);
            }
            return lists.containsAll(names);
        } catch (final SecurityException e) {
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("name = ");
        description.appendValueList("[", ", ", "]", names);
    }

}
