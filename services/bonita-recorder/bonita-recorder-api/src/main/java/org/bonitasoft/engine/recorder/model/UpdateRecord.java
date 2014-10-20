/*
 *
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.bonitasoft.engine.recorder.model;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Baptiste Mesta
 */
public final class UpdateRecord extends Record {

    private Map<String, Object> fields;

    private UpdateRecord(final PersistentObject entity) {
        super(entity);
    }

    public static UpdateRecord buildSetFields(final PersistentObject entity, final Map<String, Object> fields) {
        final UpdateRecord updateRecord = new UpdateRecord(entity);
        updateRecord.addFields(fields);
        return updateRecord;
    }

    public static UpdateRecord buildSetFields(final PersistentObject entity, final EntityUpdateDescriptor descriptor) {
        NullCheckingUtil.checkArgsNotNull(descriptor);
        final UpdateRecord updateRecord = new UpdateRecord(entity);
        updateRecord.addFields(descriptor.getFields());
        return updateRecord;
    }

    public void addField(final String fieldName, final Object fieldValue) {
        if (fields == null) {
            fields = new HashMap<String, Object>();
        }
        fields.put(fieldName, fieldValue);
    }

    public void addFields(final Map<String, Object> fields) {
        if (this.fields == null) {
            this.fields = new HashMap<String, Object>();
        }
        this.fields.putAll(fields);
    }

    public Map<String, Object> getFields() {
        return fields;
    }

}
