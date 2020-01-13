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
package org.bonitasoft.engine.core.document.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author Baptiste Mesta
 */
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "document_mapping")
@Cacheable(false)
public class SMappedDocument extends AbstractSMappedDocument {

    public SMappedDocument(AbstractSDocumentMapping documentMapping, SDocument document) {
        this.setId(documentMapping.getId());
        this.setName(documentMapping.getName());
        this.setDescription(documentMapping.getDescription());
        this.setVersion(documentMapping.getVersion());
        this.setDocumentId(documentMapping.getDocumentId());
        this.setProcessInstanceId(documentMapping.getProcessInstanceId());
        this.setIndex(documentMapping.getIndex());
        this.document = SLightDocument.builder()
                .id(document.getId())
                .tenantId(document.getTenantId())
                .fileName(document.getFileName())
                .hasContent(document.hasContent())
                .mimeType(document.getMimeType())
                .author(document.getAuthor())
                .url(document.getUrl())
                .creationDate(document.getCreationDate())
                .build();
    }

    public AbstractSDocument getDocument() {
        return document;
    }

    public void setDocument(SLightDocument document) {
        this.document = document;
    }

    public long getAuthor() {
        return document.getAuthor();
    }

    public long getCreationDate() {
        return document.getCreationDate();
    }

    public String getMimeType() {
        return document.getMimeType();
    }

    public String getFileName() {
        return document.getFileName();
    }

    public boolean hasContent() {
        return document.hasContent();
    }

    public String getUrl() {
        return document.getUrl();
    }

}
