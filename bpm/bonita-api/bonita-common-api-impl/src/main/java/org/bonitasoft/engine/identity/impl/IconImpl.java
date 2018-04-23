/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.identity.impl;

import java.util.Arrays;
import java.util.Objects;

import org.bonitasoft.engine.identity.Icon;

/**
 * @author Baptiste Mesta
 */
public class IconImpl implements Icon {

    private long id;
    private String mimeType;
    private byte[] content;

    public IconImpl(long id, String mimeType, byte[] content) {
        this.id = id;
        this.mimeType = mimeType;
        this.content = content;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IconImpl icon = (IconImpl) o;
        return id == icon.id &&
                Objects.equals(mimeType, icon.mimeType) &&
                Arrays.equals(content, icon.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mimeType, content);
    }

    @Override
    public String toString() {
        return "IconImpl{" +
                "mimeType='" + mimeType + '\'' +
                ", id=" + id +
                '}';
    }
}
