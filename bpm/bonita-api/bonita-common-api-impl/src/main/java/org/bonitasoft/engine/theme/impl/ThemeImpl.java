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
package org.bonitasoft.engine.theme.impl;

import org.bonitasoft.engine.bpm.internal.BaseElementImpl;
import org.bonitasoft.engine.theme.Theme;
import org.bonitasoft.engine.theme.ThemeType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Celine Souchet
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ThemeImpl extends BaseElementImpl implements Theme {

    private static final long serialVersionUID = 5445403438892593799L;

    private byte[] content;

    private byte[] cssContent;

    private boolean isDefault;

    private Date lastUpdateDate;

    private ThemeType type;

    public ThemeImpl(final byte[] content, final byte[] cssContent, final boolean isDefault, final ThemeType type, final Date lastUpdateDate) {
        super();
        this.content = content;
        this.cssContent = cssContent;
        this.isDefault = isDefault;
        this.lastUpdateDate = lastUpdateDate;
        this.type = type;
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
    public Date getLastUpdatedDate() {
        return lastUpdateDate;
    }

    public void setLastUpdatedDate(final Date lastUpdatedDate) {
        lastUpdateDate = lastUpdatedDate;
    }

    @Override
    public ThemeType getType() {
        return type;
    }

    public void setType(final ThemeType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (type == null ? 0 : type.hashCode());
        result = prime * result + (lastUpdateDate == null ? 0 : lastUpdateDate.hashCode());
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

        final ThemeImpl other = (ThemeImpl) obj;
        if (!super.equals(obj)) {
            return false;
        }
        if (type == null && other.type != null) {
            return false;
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (lastUpdateDate == null && other.lastUpdateDate != null) {
            return false;
        } else if (!lastUpdateDate.equals(other.lastUpdateDate)) {
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
    public String toString() {
        return "ThemeImpl [id=" + getId() + ", content=" + Arrays.toString(content) + ", cssContent=" + Arrays.toString(cssContent) + ", isDefault="
                + isDefault + ", lastUpdateDate=" + lastUpdateDate + ", type=" + type + "]";
    }

}
