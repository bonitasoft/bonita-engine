/*******************************************************************************
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.impl;

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
