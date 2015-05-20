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
package org.bonitasoft.engine.bpm.document.impl;

import java.util.Objects;

import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Baptiste Mesta
 */
public class DocumentDefinitionImpl extends NamedElementImpl implements DocumentDefinition {

    private static final long serialVersionUID = 2L;

    private String url;
    private String file;
    private String mimeType;
    private String description;
    private String fileName;
    private Expression initialValue;

    /**
     * @param name the name of the document
     */
    public DocumentDefinitionImpl(final String name) {
        super(name);
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
    public String getContentMimeType() {
        return mimeType;
    }

    /**
     * @param description the description of the document
     */
    public void setDescription(final String description) {
        this.description = description;

    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setFile(final String file) {
        this.file = file;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public void setInitialValue(Expression initialValue) {
        this.initialValue = initialValue;
    }

    public Expression getInitialValue() {
        return initialValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        DocumentDefinitionImpl that = (DocumentDefinitionImpl) o;
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

    @Override
    public String toString() {
        return "DocumentDefinitionImpl{" +
                "url='" + url + '\'' +
                ", file='" + file + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", description='" + description + '\'' +
                ", fileName='" + fileName + '\'' +
                ", initialValue=" + initialValue +
                "} " + super.toString();
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }
}
