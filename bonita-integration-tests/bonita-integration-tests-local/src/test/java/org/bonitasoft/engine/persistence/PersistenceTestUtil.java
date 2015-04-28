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
package org.bonitasoft.engine.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.persistence.model.Human;
import org.bonitasoft.engine.persistence.model.Parent;

public class PersistenceTestUtil {

    protected static void checkHuman(final Human expected, final Human actual) {
        assertThat(actual).as("Human").isNotNull();
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
        assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
        assertThat(actual.getAge()).isEqualTo(expected.getAge());
    }

    protected static Human buildHuman(final Human src) {
        final Human human = buildHuman(src.getFirstName(), src.getLastName(), src.getAge());
        human.setId(src.getId());
        return human;
    }

    protected static Human buildHuman(final String firstName, final String lastName, final int age) {
        final Human human = new Human();
        human.setFirstName(firstName);
        human.setLastName(lastName);
        human.setAge(age);
        return human;
    }

    protected static Parent buildParent(final String firstName, final String lastName, final int age) {
        final Parent parent = new Parent();
        parent.setFirstName(firstName);
        parent.setLastName(lastName);
        parent.setAge(age);
        return parent;
    }

    protected static Map<String, Object> getMap(final String key, final Object value) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(key, value);
        return map;
    }

}
