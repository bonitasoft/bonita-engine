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
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * @author Baptiste Mesta
 */
public class ListElementMatcher extends BaseMatcher<List<?>> {

    private String[] strings;

    private Object[] longs;

    private enum TYPE {
        NAME(String.class,
             "getName"),
        ID(long.class,
           "getId"),
        VERSION(String.class,
                "getVersion"),
        DESCRIPTION(String.class,
                    "getDescription"),
        USERNAME(String.class,
                 "getUserName"),
        MANAGER(long.class,
                "getManagerUserId"),
        STATE(String.class,
              "getState");

        private final Class<?> clazz;

        private final String getter;

        private TYPE(final Class<?> clazz, final String getter) {
            this.clazz = clazz;
            this.getter = getter;

        }

        public Class<?> getClazz() {
            return clazz;
        }

        public String getGetter() {
            return getter;
        }
    }

    private final TYPE type;

    public ListElementMatcher(final TYPE type, final String... strings) {
        this.strings = strings;
        this.type = type;
    }

    public ListElementMatcher(final TYPE type, final Long[] longs) {
        this.longs = longs;
        this.type = type;
    }

    public static ListElementMatcher nameAre(final String... names) {
        return new ListElementMatcher(TYPE.NAME, names);
    }

    public static ListElementMatcher versionAre(final String... names) {
        return new ListElementMatcher(TYPE.VERSION, names);
    }

    public static ListElementMatcher usernamesAre(final String... names) {
        return new ListElementMatcher(TYPE.USERNAME, names);
    }

    public static ListElementMatcher managersAre(final Long... ids) {
        return new ListElementMatcher(TYPE.MANAGER, ids);
    }

    public static ListElementMatcher idAre(final Long... ids) {
        return new ListElementMatcher(TYPE.ID, ids);
    }

    public static ListElementMatcher stateAre(final String... states) {
        return new ListElementMatcher(TYPE.STATE, states);
    }

    @Override
    public boolean matches(final Object item) {
        final List<?> list = (List<?>) item;
        try {
            boolean match = true;
            final Class<?> clazz = type.getClazz();
            final Object[] expected;
            if (clazz == String.class) {
                expected = strings;
            } else {
                expected = longs;
            }
            if (expected.length != list.size()) {
                return false;
            }
            final Method m = list.get(0).getClass().getMethod(type.getGetter());
            for (int i = 0; i < expected.length; i++) {
                final Object invoke = m.invoke(list.get(i), null);
                match &= expected[i] == null && invoke == null || expected[i].equals(invoke);
                if (!match) {
                    return false;
                }
            }
            return match;
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
        final Class<?> clazz = type.getClazz();
        final Object[] expected;
        if (clazz == String.class) {
            expected = strings;
        } else {
            expected = longs;
        }
        description.appendText(type.name());
        description.appendText(" = ");
        description.appendValueList("[", ", ", "]", expected);
    }
}
