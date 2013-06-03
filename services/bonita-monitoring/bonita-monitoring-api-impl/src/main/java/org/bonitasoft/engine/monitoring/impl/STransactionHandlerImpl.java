/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.monitoring.impl;

import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;

/**
 * @author Christophe Havard
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class STransactionHandlerImpl implements SHandler<SEvent> {

    static final String TRANSACTION_ACTIVE_EVT = "TRANSACTION_ACTIVE";

    static final String TRANSACTION_COMMITED_EVT = "TRANSACTION_COMMITED";

    static final String TRANSACTION_ROLLEDBACK_EVT = "TRANSACTION_ROLLEDBACK";

    private int numberOfactiveTransactions = 0;

    public int getNumberOfActiveTransactions() {
        return numberOfactiveTransactions;
    }

    @Override
    public synchronized void execute(final SEvent event) {
        final String eventType = event.getType();
        if (eventType.compareToIgnoreCase(TRANSACTION_ACTIVE_EVT) == 0) {
            numberOfactiveTransactions++;
        } else if (eventType.compareToIgnoreCase(TRANSACTION_COMMITED_EVT) == 0) {
            numberOfactiveTransactions--;
        } else if (eventType.compareToIgnoreCase(TRANSACTION_ROLLEDBACK_EVT) == 0) {
            numberOfactiveTransactions--;
        }
    }

    @Override
    public boolean isInterested(final SEvent event) {
        final String eventType = event.getType();
        if (eventType.compareToIgnoreCase(TRANSACTION_ACTIVE_EVT) == 0) {
            return true;
        } else if (eventType.compareToIgnoreCase(TRANSACTION_COMMITED_EVT) == 0) {
            return true;
        } else if (eventType.compareToIgnoreCase(TRANSACTION_ROLLEDBACK_EVT) == 0) {
            return true;
        }
        return false;
    }

}
