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
package org.bonitasoft.engine.identity.model.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserLogin;

/**
 * contains last connection date
 *
 *
 * sequence id 27
 * @author Baptiste Mesta
 */
@Data
@NoArgsConstructor
@ToString(exclude = "sUser")
@EqualsAndHashCode(exclude = "sUser")
public class SUserLoginImpl implements SUserLogin {

    private long id;
    private long tenantId;
    private Long lastConnection;
    private SUser sUser;

    public SUserLoginImpl(Long lastConnection) {
        this.lastConnection = lastConnection;
    }

}
