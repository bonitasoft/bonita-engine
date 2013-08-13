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
 * @author Matthieu Chaffotte
 */
public class SUserHandlerImpl implements SHandler<SEvent> {

    private static final long serialVersionUID = 1L;

    public static final String USER_CREATED = "USER_CREATED";

    public static final String USER_DELETED = "USER_DELETED";

    private long nbOfUsers = 0;

    public void setNbOfUsers(final long nbOfUsers) {
        this.nbOfUsers = nbOfUsers;
    }

    @Override
    public void execute(final SEvent event) {
        final String type = event.getType();
        if (USER_CREATED.compareToIgnoreCase(type) == 0) {
            nbOfUsers++;
        } else if (USER_DELETED.compareToIgnoreCase(type) == 0) {
            nbOfUsers--;
        }
    }

    @Override
    public boolean isInterested(final SEvent event) {
        final String type = event.getType();
        if (USER_CREATED.compareToIgnoreCase(type) == 0) {
            return true;
        } else if (USER_DELETED.compareToIgnoreCase(type) == 0) {
            return true;
        }
        return false;
    }

    public long getNbOfUsers() {
        return nbOfUsers;
    }

}
