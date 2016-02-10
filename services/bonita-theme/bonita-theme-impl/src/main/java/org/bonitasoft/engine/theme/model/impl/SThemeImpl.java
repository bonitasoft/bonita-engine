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
package org.bonitasoft.engine.theme.model.impl;

import java.util.Arrays;

import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;

/**
 * @author Celine Souchet
 */
public class SThemeImpl extends PersistentObjectId implements STheme {

    private static final long serialVersionUID = -7340761293800942582L;

    private byte[] content;

    private byte[] cssContent;

    private boolean isDefault;

    private long lastUpdateDate;

    private SThemeType type;

    public SThemeImpl() {
        super();
    }

    public SThemeImpl(final byte[] content, final boolean isDefault, final SThemeType type, final long lastUpdatedDate) {
        super();
        this.content = content;
        this.isDefault = isDefault;
        lastUpdateDate = lastUpdatedDate;
        this.type = type;
    }

    public SThemeImpl(final STheme theme) {
        this(theme.getContent(), theme.isDefault(), theme.getType(), theme.getLastUpdateDate());
        cssContent = theme.getCssContent();
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    public void setContent(final byte[] content) {
        this.content = content;
    }

    @Override
    public byte[] getCssContent() {
        return cssContent;
    }

    public void setCssContent(final byte[] cssContent) {
        this.cssContent = cssContent;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(final boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(final long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public SThemeType getType() {
        return type;
    }

    public void setType(final SThemeType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (type == null ? 0 : type.hashCode());
        result = prime * result + (int) (lastUpdateDate ^ lastUpdateDate >>> 32);
        result = prime * result + (isDefault ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(content);
        result = prime * result + Arrays.hashCode(cssContent);
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
        final SThemeImpl other = (SThemeImpl) obj;
        if (!super.equals(obj)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (lastUpdateDate != other.lastUpdateDate) {
            return false;
        }
        if (!Arrays.equals(content, other.content)) {
            return false;
        }
        if (!Arrays.equals(cssContent, other.cssContent)) {
            return false;
        }
        return true;
    }

    @Override
    public String getDiscriminator() {
        return STheme.class.getName();
    }

    @Override
    public String toString() {
        return "SThemeImpl [id=" + getId() + ", content=" + Arrays.toString(content) + ", cssContent=" + Arrays.toString(cssContent) + ", isDefault="
                + isDefault + ", lastUpdateDate=" + lastUpdateDate + ", type=" + type + "]";
    }
}
