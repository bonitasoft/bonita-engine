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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "business_app_menu")
@IdClass(PersistentObjectId.class)
public class SApplicationMenu implements PersistentObject {

    public static String ID = "id";
    public static String DISPLAY_NAME = "displayName";
    public static String APPLICAITON_ID = "applicationId";
    public static String APPLICATION_PAGE_ID = "applicationPageId";
    public static String PARENT_ID = "parentId";
    public static String INDEX = "index";
    @Id
    private long id;
    @Id
    private long tenantId;
    @Column
    private String displayName;
    @Column
    private long applicationId;
    @Column
    private Long applicationPageId;
    @Column
    private Long parentId;
    @Column(name = "index_")
    private int index;

    public SApplicationMenu(final String displayName, long applicationId, final Long applicationPageId,
            final int index) {
        this.displayName = displayName;
        this.applicationId = applicationId;
        this.applicationPageId = applicationPageId;
        this.index = index;
    }

}
