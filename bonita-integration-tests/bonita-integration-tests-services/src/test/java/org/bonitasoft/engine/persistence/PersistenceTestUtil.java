package org.bonitasoft.engine.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.persistence.model.Human;
import org.bonitasoft.engine.persistence.model.Parent;

public class PersistenceTestUtil {

    protected static void checkHuman(final Human expected, final Human actual) {
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getAge(), actual.getAge());
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
        human.setDeleted(false);
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
