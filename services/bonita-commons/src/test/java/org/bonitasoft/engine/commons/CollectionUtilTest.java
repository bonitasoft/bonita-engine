package org.bonitasoft.engine.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CollectionUtilTest {

    @Test(expected = UnsupportedOperationException.class)
    public void should_emptyOrUnmodifiable_return_unmodiafiable_list() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("plop");
        List<String> result = CollectionUtil.emptyOrUnmodifiable(list);

        assertEquals("plop", result.get(0));
        assertEquals(1, result.size());
        result.add("plop2");
    }

    @Test
    public void should_emptyOrUnmodifiable_return_empty_list() {
        List<String> result = CollectionUtil.emptyOrUnmodifiable(null);

        assertTrue(result.isEmpty());
    }

}
