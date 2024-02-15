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
package org.bonitasoft.engine.business.application.model;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "business_app")
@Cacheable(false)
public class SApplicationWithIcon extends AbstractSApplication {

    public static final String ICON_CONTENT = "iconContent";
    public static List<String> ALWAYS_MODIFIABLE_FIELDS = Arrays.asList(LAYOUT_ID, THEME_ID, ICON_MIME_TYPE,
            ICON_CONTENT,
            UPDATED_BY, LAST_UPDATE_DATE);

    @Type(type = "materialized_blob")
    @Column
    private byte[] iconContent;

    public SApplicationWithIcon(String token, String displayName, String version,
            long creationDate, long createdBy, String state, boolean editable) {
        super(token, displayName, version, creationDate, createdBy, state, editable);
    }

    public SApplicationWithIcon(String token, String displayName, String version,
            long creationDate, long createdBy, String state) {
        super(token, displayName, version, creationDate, createdBy, state, true);
    }

}
