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

import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserLogin;

/**
 * contains last connection date
 *
 *
 * sequence id 27
 * @author Baptiste Mesta
 */
public class SUserLoginImpl extends SPersistentObjectImpl implements SUserLogin {

    private Long lastConnection;

    private SUser sUser;


    public SUserLoginImpl() {
        super();
    }

    public SUserLoginImpl(Long lastConnection) {
        this.lastConnection = lastConnection;
    }

    public SUser getsUser() {
        return sUser;
    }

    public void setsUser(SUser sUser) {
        this.sUser = sUser;
    }

    @Override
    public Long getLastConnection() {
        return lastConnection;
    }

    public void setLastConnection(Long lastConnection) {
        this.lastConnection = lastConnection;
    }

    @Override
    public String getDiscriminator() {
        return SUserLogin.class.getName();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SUserLoginImpl)) return false;
        if (!super.equals(o)) return false;

        SUserLoginImpl that = (SUserLoginImpl) o;

        if (lastConnection != null ? !lastConnection.equals(that.lastConnection) : that.lastConnection != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (lastConnection != null ? lastConnection.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SUserLoginImpl{" +
                "lastConnection=" + lastConnection +
                '}';
    }
}
