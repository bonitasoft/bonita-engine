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
package org.bonitasoft.engine.theme.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Celine Souchet
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class STheme implements PersistentObject {

    public static final String ID = "id";
    public static final String CONTENT = "content";
    public static final String CSS_CONTENT = "cssContent";
    public static final String LAST_UPDATE_DATE = "lastUpdateDate";
    public static final String TYPE = "type";
    public static final String IS_DEFAULT = "isDefault";
    private long id;
    private long tenantId;
    private byte[] content;
    private byte[] cssContent;
    private boolean isDefault;
    private long lastUpdateDate;
    private SThemeType type;

    public STheme(final byte[] content, final boolean isDefault, final SThemeType type, final long lastUpdatedDate) {
        super();
        this.content = content;
        this.isDefault = isDefault;
        lastUpdateDate = lastUpdatedDate;
        this.type = type;
    }

    public STheme(final STheme theme) {
        this(theme.getContent(), theme.isDefault(), theme.getType(), theme.getLastUpdateDate());
        cssContent = theme.getCssContent();
    }

}
