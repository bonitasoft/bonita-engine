/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
 */
package org.bonitasoft.engine.core.document.model.impl;

import java.util.Arrays;

import org.bonitasoft.engine.core.document.model.SDocument;

/**
 * @author Nicolas Chabanoles
 * @author Baptiste Mesta
 */
public class SDocumentImpl extends SLightDocumentImpl implements SDocument {

    private static final long serialVersionUID = 3494829428880067405L;



    private byte[] content;

    public SDocumentImpl() {
    }


    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SDocumentImpl sDocument = (SDocumentImpl) o;

        if (!Arrays.equals(content, sDocument.content)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (content != null ? Arrays.hashCode(content) : 0);
        return result;
    }


}
