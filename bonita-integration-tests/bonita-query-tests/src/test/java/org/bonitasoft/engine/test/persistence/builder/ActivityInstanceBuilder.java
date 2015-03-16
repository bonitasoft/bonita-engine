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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.core.process.instance.model.impl.SActivityInstanceImpl;


/**
 * @author Julien Reboul
 *
 */
public abstract class ActivityInstanceBuilder<T extends SActivityInstanceImpl, B extends ActivityInstanceBuilder<T, B>>
        extends FlowNodeInstanceBuilder<T, B> {

    protected long abortedByBoundaryEventId = 0;
    
    public B withAbortedByBoundary(final long abortedByBoundaryEventId) {
        this.abortedByBoundaryEventId = abortedByBoundaryEventId;
        return thisBuilder;
    }

    @Override
    protected T fill(T persistent) {
        super.fill(persistent);
        persistent.setAbortedByBoundary(abortedByBoundaryEventId);
        return persistent;
    }
}
