/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
