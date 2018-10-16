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
package org.bonitasoft.engine.session.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
@Data
@Builder(toBuilder = true)
public class SSession implements Serializable {

    private long tenantId;
    private final long id;
    /**
     * creation date (GMT+0)
     */
    private Date creationDate;
    private long duration;

    /**
     * last renew date (GMT+0)
     */
    private Date lastRenewDate;
    private String userName;
    private long userId;
    //FIXME: remove it, it looks like it is never set
    private String clusterNode;
    private String applicationName;
    /**
     * true if the user is the technical user: false otherwise
     */
    private boolean technicalUser;
    private List<String> profiles;


    /**
     * @return the expiration date (GMT+0)
     */
    public Date getExpirationDate() {
        return new Date(lastRenewDate.getTime() + duration);
    }


    /**
     * @return true if the session is still valid
     */
    public boolean isValid() {
        return getExpirationDate().getTime() > System.currentTimeMillis();
    }

}
