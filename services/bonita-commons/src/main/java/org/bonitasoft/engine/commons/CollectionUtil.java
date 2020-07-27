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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CollectionUtil {

    public static Map<String, Object> buildSimpleMap(final String key, final Object value) {
        final Map<String, Object> result = new HashMap<String, Object>();
        result.put(key, value);
        return result;
    }

    public static <T> List<T> emptyOrUnmodifiable(final List<T> list) {
        return list == null ? Collections.<T> emptyList() : Collections.unmodifiableList(list);
    }

    public static <T> List<List<T>> split(List<T> source, int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("can't split a list with a chunkSize = " + chunkSize);
        }
        int size = source.size();
        if (size <= 0) {
            return Collections.emptyList();
        }
        return IntStream.range(0, size / chunkSize + (size % chunkSize == 0 ? 0 : 1))
                .mapToObj(n -> source.subList(n * chunkSize, Math.min(size, (n + 1) * chunkSize)))
                .collect(Collectors.toList());
    }
}
