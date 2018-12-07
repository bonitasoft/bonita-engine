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
package org.bonitasoft.engine.identity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Anthony Birembaut
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SCustomUserInfoValue implements PersistentObject {

    public static final String ID = "id";
    public static final String USER_ID = "userId";
    public static final String DEFINITION_ID = "definitionId";
    public static final String VALUE = "value";
    private long id;
    private long tenantId;
    protected long userId;
    protected long definitionId;
    protected String value;

}
