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
package org.bonitasoft.engine.commons;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class CollectionUtilTest {

    @Test
    void should_emptyOrUnmodifiable_return_unmodifiable_list() {
        ArrayList<String> list = new ArrayList<>();
        list.add("plop");
        List<String> result = CollectionUtil.emptyOrUnmodifiable(list);

        assertEquals("plop", result.get(0));
        assertEquals(1, result.size());
        assertThrows(UnsupportedOperationException.class, () -> result.add("plop2"));
    }

    @Test
    void should_emptyOrUnmodifiable_return_empty_list() {
        List<String> result = CollectionUtil.emptyOrUnmodifiable(null);
        assertTrue(result.isEmpty());
    }

}
