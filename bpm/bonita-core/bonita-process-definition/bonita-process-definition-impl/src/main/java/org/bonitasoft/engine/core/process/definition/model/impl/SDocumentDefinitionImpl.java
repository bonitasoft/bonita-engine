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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.Objects;

import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.core.process.definition.model.SDocumentDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 */
public class SDocumentDefinitionImpl extends SNamedElementImpl implements SDocumentDefinition {

    private static final long serialVersionUID = 6622182823980202155L;

    private String url;
    private String file;
    private String mimeType;
    private String description;
    private String fileName;
    private SExpression initialValue;

    /**
     * @param name
     */
    public SDocumentDefinitionImpl(final String name) {
        super(name);
    }

    /**
     * @param documentDefinition
     */
    public SDocumentDefinitionImpl(final DocumentDefinition documentDefinition) {
        super(documentDefinition.getName());
        url = documentDefinition.getUrl();
        file = documentDefinition.getFile();
        description = documentDefinition.getDescription();
        mimeType = documentDefinition.getContentMimeType();
        fileName = documentDefinition.getFileName();
        initialValue = ServerModelConvertor.convertExpression(documentDefinition.getInitialValue());
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getFile() {
        return file;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setFile(final String file) {
        this.file = file;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    public SExpression getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(SExpression initialValue) {
        this.initialValue = initialValue;
    }

    @Override
    public String toString() {
        return "SDocumentDefinitionImpl{" +
                "url='" + url + '\'' +
                ", file='" + file + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", description='" + description + '\'' +
                ", fileName='" + fileName + '\'' +
                ", initialValue=" + initialValue +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SDocumentDefinitionImpl that = (SDocumentDefinitionImpl) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(file, that.file) &&
                Objects.equals(mimeType, that.mimeType) &&
                Objects.equals(description, that.description) &&
                Objects.equals(fileName, that.fileName) &&
                Objects.equals(initialValue, that.initialValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), url, file, mimeType, description, fileName, initialValue);
    }
}
