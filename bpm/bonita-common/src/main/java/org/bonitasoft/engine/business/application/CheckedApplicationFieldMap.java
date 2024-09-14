/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.application;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A Map which first checks that the key {@link ApplicationField} is correct.
 */
class CheckedApplicationFieldMap implements Map<ApplicationField, Serializable> {

    private final Map<ApplicationField, Serializable> m;
    private final Predicate<ApplicationField> isValidKey;

    CheckedApplicationFieldMap(Map<ApplicationField, Serializable> m, Predicate<ApplicationField> isValidKey) {
        this.m = Objects.requireNonNull(m);
        this.isValidKey = Objects.requireNonNull(isValidKey);
    }

    private ApplicationField checkKey(ApplicationField key) {
        if (!isValidKey.test(key)) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Attempt to insert {0} in a specialized map which does not support it.",
                            key.name()));
        }
        return key;
    }

    @Override
    public int size() {
        return m.size();
    }

    @Override
    public boolean isEmpty() {
        return m.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return m.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return m.containsValue(value);
    }

    @Override
    public Serializable get(Object key) {
        return m.get(key);
    }

    @Override
    public Serializable put(ApplicationField key, Serializable value) {
        return m.put(checkKey(key), value);
    }

    @Override
    public Serializable remove(Object key) {
        return m.remove(key);
    }

    @Override
    public void putAll(Map<? extends ApplicationField, ? extends Serializable> map) {
        map.keySet().forEach(this::checkKey);
        m.putAll(map);
    }

    @Override
    public void clear() {
        m.clear();
    }

    @Override
    public Set<ApplicationField> keySet() {
        return m.keySet();
    }

    @Override
    public Collection<Serializable> values() {
        return m.values();
    }

    @Override
    public Set<Entry<ApplicationField, Serializable>> entrySet() {
        return m.entrySet();
    }

}
