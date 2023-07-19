/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.platform;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.temporary.content.TemporaryContentService;
import org.bonitasoft.engine.transaction.TransactionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TemporaryContentScheduler {

    private final TemporaryContentService temporaryContentService;
    private final TransactionService transactionService;

    public TemporaryContentScheduler(TransactionService transactionService,
            TemporaryContentService temporaryContentService) {
        this.transactionService = transactionService;
        this.temporaryContentService = temporaryContentService;
    }

    /*
     * Scheduled task to delete outdated temporary content
     * Runs in fixeDelay defined by the "bonita.runtime.temporary-content.cleanup.delay" property
     * @return a number of deleted Temp Files
     */
    @Scheduled(fixedDelayString = "${bonita.runtime.temporary-content.cleanup.delay:PT1H}")
    public int cleanOutDatedTempFiles() {
        try {
            int nbrDeletedFiles = transactionService
                    .executeInTransaction(temporaryContentService::cleanOutDatedTemporaryContent);
            log.info("{} outdated temporary Content has been deleted", nbrDeletedFiles);
            return nbrDeletedFiles;
        } catch (Exception e) {
            log.error("Outdated temporary Content cleanup failed");
            throw new BonitaRuntimeException(e);
        }
    }
}
