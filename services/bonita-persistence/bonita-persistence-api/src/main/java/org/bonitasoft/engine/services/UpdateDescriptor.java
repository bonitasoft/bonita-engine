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
package org.bonitasoft.engine.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Charles Souillard
 * @author Emmanuel Duchastenier
 */
public class UpdateDescriptor {

    private static final String ID = "id";

    private final PersistentObject entity;

    private Map<String, Object> fields;

    public UpdateDescriptor(final PersistentObject entity) {
        super();
        this.entity = entity;
    }

    public static UpdateDescriptor buildSetField(final PersistentObject entity, final String fieldName, final Object fieldValue) {
        final UpdateDescriptor updateDescriptor = new UpdateDescriptor(entity);
        updateDescriptor.addField(fieldName, fieldValue);
        return updateDescriptor;
    }

    public static UpdateDescriptor buildSetFields(final PersistentObject entity, final Map<String, Object> fields) {
        final UpdateDescriptor updateDescriptor = new UpdateDescriptor(entity);
        updateDescriptor.addFields(fields);
        return updateDescriptor;
    }

    public void addField(final String fieldName, final Object fieldValue) {
        if (fields == null) {
            fields = new HashMap<String, Object>();
        }
        if (ID.equalsIgnoreCase(fieldName)) {
            throw new RuntimeException("Updating an object's " + ID + " field is forbidden");
        }
        fields.put(fieldName, fieldValue);
    }

    public void addFields(final Map<String, Object> fields) {
        if (this.fields == null) {
            this.fields = new HashMap<String, Object>();
        }
        if (fields.containsKey(ID)) {
            throw new RuntimeException("Updating an object's " + ID + " field is forbidden");
        }
        this.fields.putAll(fields);
    }

    public PersistentObject getEntity() {
        return entity;
    }

    public Map<String, Object> getFields() {
        if (fields == null) {
            return Collections.emptyMap();
        }
        return fields;
    }

    @Override
    public String toString() {
        return "UpdateDescriptor [entity=" + entity + ", fields=" + fields + "]";
    }

}
