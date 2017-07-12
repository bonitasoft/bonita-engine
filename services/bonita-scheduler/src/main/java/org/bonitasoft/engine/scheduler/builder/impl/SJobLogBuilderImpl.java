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
package org.bonitasoft.engine.scheduler.builder.impl;

import org.bonitasoft.engine.scheduler.builder.SJobLogBuilder;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.impl.SJobLogImpl;

/**
 * 
 * @author Celine Souchet
 * 
 */
public class SJobLogBuilderImpl implements SJobLogBuilder {

    private final SJobLogImpl entity;
    
    public SJobLogBuilderImpl(final SJobLogImpl entity) {
        super();
        this.entity = entity;
    }

    @Override
    public SJobLogBuilder setLastUpdateDate(final Long lastUpdateDate) {
        entity.setLastUpdateDate(lastUpdateDate);
        return this;
    }

    @Override
    public SJobLogBuilder setLastMessage(final String lastMessage) {
        entity.setLastMessage(lastMessage);
        return this;
    }

    @Override
    public SJobLog done() {
        return entity;
    }

}
