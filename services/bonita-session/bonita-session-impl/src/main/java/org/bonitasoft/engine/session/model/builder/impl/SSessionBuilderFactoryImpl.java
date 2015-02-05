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

import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.session.model.builder.SSessionBuilder;
import org.bonitasoft.engine.session.model.builder.SSessionBuilderFactory;
import org.bonitasoft.engine.session.model.impl.SSessionImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SSessionBuilderFactoryImpl implements SSessionBuilderFactory {

    @Override
    public SSessionBuilder createNewInstance(final long id, final long tenantId, final long duration, final String username, 
            final String applicationName, final long userId) {
        final SSessionImpl entity = new SSessionImpl(id, tenantId, username, applicationName, userId);
        entity.setDuration(duration);
        return new SSessionBuilderImpl(entity);
    }

    @Override
    public SSession copy(final SSession session) {
        return new SSessionImpl(session);
    }

}
