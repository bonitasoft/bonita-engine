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
