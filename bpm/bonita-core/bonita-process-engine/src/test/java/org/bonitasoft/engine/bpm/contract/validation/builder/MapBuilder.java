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
package org.bonitasoft.engine.bpm.contract.validation.builder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.data.MapEntry;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class MapBuilder {

    public static Builder<String, Serializable> aMap() {
        return ImmutableMap.<String, Serializable> builder();
    }

    public static Map<String, Serializable> contractInputMap(final MapEntry... entries) {
        final Map<String, Serializable> result = new HashMap<>();
        for (final MapEntry entry : entries) {
            result.put((String) entry.key, (Serializable) entry.value);
        }
        return result;
    }
}
