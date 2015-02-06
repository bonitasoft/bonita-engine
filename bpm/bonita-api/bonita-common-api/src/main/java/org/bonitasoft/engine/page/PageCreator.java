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
package org.bonitasoft.engine.page;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Laurent Leseigneur
 */
public class PageCreator implements Serializable {

    public enum PageField {
        NAME, DISPLAY_NAME, DESCRIPTION, CONTENT_NAME;
    }

    private static final long serialVersionUID = 8174091386958635983L;

    private final Map<PageField, Serializable> fields;

    public PageCreator(final String name, final String contentName) {
        fields = new HashMap<PageField, Serializable>(2);
        fields.put(PageField.NAME, name);
        fields.put(PageField.CONTENT_NAME, contentName);
    }

    public String getName() {
        return fields.get(PageField.NAME).toString();
    }

    public PageCreator setDescription(final String description) {
        fields.put(PageField.DESCRIPTION, description);
        return this;
    }

    public PageCreator setDisplayName(final String displayName) {
        fields.put(PageField.DISPLAY_NAME, displayName);
        return this;
    }

    public Map<PageField, Serializable> getFields() {
        return fields;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (fields == null ? 0 : fields.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PageCreator other = (PageCreator) obj;
        if (fields == null) {
            if (other.fields != null) {
                return false;
            }
        } else if (!fields.equals(other.fields)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PageCreator [fields=" + fields + "]";
    }

}
