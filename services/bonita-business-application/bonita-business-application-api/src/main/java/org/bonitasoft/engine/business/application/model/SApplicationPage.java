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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SApplicationPage implements PersistentObject {

    public static final String ID = "id";
    public static final String TOKEN = "token";
    public static final String PAGE_ID = "pageId";
    public static final String APPLICATION_ID = "applicationId";
    private long id;
    private long tenantId;
    private long applicationId;
    private long pageId;
    private String token;

    public SApplicationPage(final long applicationId, final long pageId, final String token) {
        super();
        this.applicationId = applicationId;
        this.pageId = pageId;
        this.token = token;
    }

}
