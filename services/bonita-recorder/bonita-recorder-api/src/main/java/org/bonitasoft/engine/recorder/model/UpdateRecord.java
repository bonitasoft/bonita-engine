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
