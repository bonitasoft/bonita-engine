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
package org.bonitasoft.engine.session.model.builder.impl;

import java.util.Date;

import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.session.model.builder.SSessionBuilder;
import org.bonitasoft.engine.session.model.impl.SSessionImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SSessionBuilderImpl implements SSessionBuilder {

    private final SSessionImpl entity;

    public SSessionBuilderImpl(final SSessionImpl entity) {
        super();
        this.entity = entity;
    }

    @Override
    public SSessionBuilder lastRenewDate(final Date lastRenewDate) {
        entity.setLastRenewDate(lastRenewDate);
        return this;
    }

    @Override
    public SSessionBuilder technicalUser(final boolean technicalUser) {
        entity.setTechnicalUser(technicalUser);
        return this;
    }

    @Override
    public SSession done() {
        final Date now = new Date();
        entity.setCreationDate(now);
        entity.setLastRenewDate(now);
        return entity;
    }

}
