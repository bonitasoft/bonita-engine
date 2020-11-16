/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.tenant.restart;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.profile.DefaultProfilesUpdater;
import org.bonitasoft.engine.profile.ProfilesImporter;
import org.bonitasoft.engine.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Check that default profiles are up to date
 *
 * @author Baptiste Mesta
 */
@Component
public class ProfilesUpdaterRestartHandler implements TenantRestartHandler {

    private Long tenantId;
    private TechnicalLoggerService logger;
    private ProfilesImporter profileImporter;
    private TransactionService transactionService;

    public ProfilesUpdaterRestartHandler(@Value("${tenantId}") Long tenantId,
            TechnicalLoggerService logger,
            ProfilesImporter profileImporter, TransactionService transactionService) {
        this.tenantId = tenantId;
        this.logger = logger;
        this.profileImporter = profileImporter;
        this.transactionService = transactionService;
    }

    @Override
    public void beforeServicesStart() {
    }

    @Override
    public void afterServicesStart() {
        // Update default profiles (with a new transaction)
        try {
            transactionService.executeInTransaction((Callable<Void>) () -> {
                getDefaultProfilesUpdater().execute();
                return null;
            });
        } catch (Exception e) {
            logger.log(ProfilesUpdaterRestartHandler.class, TechnicalLogSeverity.ERROR,
                    "Unable to update default profiles", e);
        }
    }

    protected DefaultProfilesUpdater getDefaultProfilesUpdater() {
        return new DefaultProfilesUpdater(tenantId, logger, profileImporter);
    }

}
