/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Test
    void should_split_a_list_of_9_elements_in_lists_of_3() {
        List<Integer> original = asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

        List<List<Integer>> split = CollectionUtil.split(original, 3);

        assertThat(split).containsExactly(asList(1, 2, 3), asList(4, 5, 6), asList(7, 8, 9));
    }

    @Test
    void should_split_a_list() {
        List<Integer> original = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        List<List<Integer>> split = CollectionUtil.split(original, 3);

        assertThat(split).containsExactly(asList(1, 2, 3), asList(4, 5, 6), asList(7, 8, 9), asList(10));
    }

    @Test
    void should_split_a_list_in_lists_of_2() {
        List<Integer> original = asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

        List<List<Integer>> split = CollectionUtil.split(original, 2);

        assertThat(split).containsExactly(asList(1, 2), asList(3, 4), asList(5, 6), asList(7, 8), asList(9));
    }

    @Test
    void should_split_a_list_of_110_elements_in_lists_of_100() {
        List<Integer> original = IntStream.range(0, 110).boxed().collect(Collectors.toList());

        List<List<Integer>> split = CollectionUtil.split(original, 100);

        assertThat(split.get(0)).isEqualTo(IntStream.range(0, 100).boxed().collect(Collectors.toList()));
        assertThat(split.get(1)).isEqualTo(IntStream.range(100, 110).boxed().collect(Collectors.toList()));
    }

    @Test
    void should_split_an_empty_list() {
        List<Integer> original = Collections.emptyList();

        List<List<Integer>> split = CollectionUtil.split(original, 3);

        assertThat(split).isEmpty();
    }

}
