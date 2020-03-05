/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.xa;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class XAResourceIsSameRMOverride implements XAResource {

    private XAResource xaResource;

    private XAResourceIsSameRMOverride(XAResource xaResource) {
        this.xaResource = xaResource;
    }

    static XAResource overrideSameRM(XAResource xaResource) {
        return new XAResourceIsSameRMOverride(xaResource);
    }

    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        xaResource.commit(xid, b);
    }

    @Override
    public void end(Xid xid, int i) throws XAException {
        xaResource.end(xid, i);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        xaResource.forget(xid);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return xaResource.getTransactionTimeout();
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        // always returns false to make it work on oracle
        return false;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        return xaResource.prepare(xid);
    }

    @Override
    public Xid[] recover(int i) throws XAException {
        return xaResource.recover(i);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        xaResource.rollback(xid);
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException {
        return xaResource.setTransactionTimeout(i);
    }

    @Override
    public void start(Xid xid, int i) throws XAException {
        xaResource.start(xid, i);
    }
}
