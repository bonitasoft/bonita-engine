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
package org.bonitasoft.engine.core.document.model.archive;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bonitasoft.engine.core.document.model.AbstractSDocumentMapping;
import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "arch_document_mapping")
@Cacheable(false)
public class SADocumentMapping extends AbstractSDocumentMapping implements ArchivedPersistentObject {

    public static final String ID = "id";
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";
    public static final String ARCHIVE_DATE = "archiveDate";
    public static final String SOURCE_OBJECT_ID = "sourceObjectId";
    public static final String DOCUMENT_ID = "documentId";
    public static final String URL = "url";
    public static final String NAME = "name";
    public static final String HAS_CONTENT = "hasContent";
    public static final String AUTHOR = "author";
    public static final String FILE_NAME = "fileName";
    public static final String MIME_TYPE = "mimeType";
    public static final String CREATION_DATE = "creationDate";
    public static final String DESCRIPTION = "description";
    public static final String VERSION = "version";
    public static final String INDEX = "index";

    private long archiveDate;
    private long sourceObjectId;

    public SADocumentMapping(final long documentId, final long processInstanceId, final long archiveDate,
            final long sourceObjectId, final String name,
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
