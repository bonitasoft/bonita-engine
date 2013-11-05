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
package org.bonitasoft.engine.api.impl.transaction.data;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.data.DataService;
import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.data.model.SDataSourceState;
import org.bonitasoft.engine.data.model.builder.SDataSourceBuilderFactory;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;

/**
 * @author Feng Hui
 */
public class CreateDefaultDataSource implements TransactionContent {

    private final DataService dataService;

    private final SessionService sessionService;

    private final long tenantId;

    private final String username;

    public CreateDefaultDataSource(final DataService dataService, final SessionService sessionService,
            final long tenantId, final String username) {
        this.dataService = dataService;
        this.sessionService = sessionService;
        this.username = username;
        this.tenantId = tenantId;

    }

    @Override
    public void execute() throws SBonitaException {
        // pass -1 because the user is the technical user, which is inexistant in DB:
        final SSession session = sessionService.createSession(tenantId, username);
        final SDataSource bonitaDataSource = BuilderFactory.get(SDataSourceBuilderFactory.class)
                .createNewInstance("bonita_data_source", "6.0", SDataSourceState.ACTIVE, "org.bonitasoft.engine.data.instance.DataInstanceDataSourceImpl")
                .done();
        dataService.createDataSource(bonitaDataSource);

        final SDataSource transientDataSource = BuilderFactory.get(SDataSourceBuilderFactory.class)
                .createNewInstance("bonita_transient_data_source", "6.0", SDataSourceState.ACTIVE,
                        "org.bonitasoft.engine.core.data.instance.impl.TransientDataInstanceDataSource").done();
        dataService.createDataSource(transientDataSource);
        sessionService.deleteSession(session.getId());
    }

}
