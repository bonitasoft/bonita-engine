/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.work;

/**
 * A runnable that notify a listener of it's state
 * 
 * @author Baptiste Mesta
 */
public abstract class NotifyingRunnable implements Runnable {

    private final RunnableListener runnableListener;

    private final long tenantId;

    public NotifyingRunnable(final RunnableListener runnableListener, final long tenantId) {
        this.runnableListener = runnableListener;
        this.tenantId = tenantId;
        runnableListener.runnableRegistered(this);
    }

    @Override
    public void run() {
        runnableListener.runnableStarted(this);
        innerRun();
        runnableListener.runnableDone(this);
    }

    public abstract void innerRun();

    public abstract void cancel();

    public long getTenantId() {
        return tenantId;
    }

}
