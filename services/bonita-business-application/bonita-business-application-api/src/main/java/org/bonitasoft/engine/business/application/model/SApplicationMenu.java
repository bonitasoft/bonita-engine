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
package org.bonitasoft.engine.business.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SApplicationMenu implements PersistentObject {

    public static String ID = "id";
    public static String DISPLAY_NAME = "displayName";
    public static String APPLICAITON_ID = "applicationId";
    public static String APPLICATION_PAGE_ID = "applicationPageId";
    public static String PARENT_ID = "parentId";
    public static String INDEX = "index";
    private long id;
    private long tenantId;
    private String displayName;
    private long applicationId;
    private Long applicationPageId;
    private Long parentId;
    private int index;


    public SApplicationMenu(final String displayName, long applicationId, final Long applicationPageId, final int index) {
        this.displayName = displayName;
        this.applicationId = applicationId;
        this.applicationPageId = applicationPageId;
        this.index = index;
    }

}
