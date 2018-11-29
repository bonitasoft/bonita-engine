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
package org.bonitasoft.engine.core.document.model.archive.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.core.document.model.archive.SADocumentMapping;
import org.bonitasoft.engine.core.document.model.impl.SDocumentMappingImpl;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Nicolas Chabanoles
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SADocumentMappingImpl extends SDocumentMappingImpl implements SADocumentMapping {

    private long archiveDate;
    private long sourceObjectId;

    public SADocumentMappingImpl(final long documentId, final long processInstanceId, final long archiveDate, final long sourceObjectId, final String name,
            final String description, final String version) {
        super(documentId, processInstanceId, name);
        setDescription(description);
        setVersion(version);
        this.archiveDate = archiveDate;
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SDocumentMapping.class;
    }

}
