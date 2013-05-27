/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.identity.OrganizationExportException;
import org.bonitasoft.engine.session.InvalidSessionException;

/**
 * @author Celine Souchet
 * 
 */
public interface OrganizationAPI extends org.bonitasoft.engine.api.OrganizationAPI {

    /**
     * Exports the organization.
     * <b>
     * An organization is composed by users, roles, groups and user memberships.
     * 
     * @return the organization contented in an XML format
     * @throws OrganizationExportException
     *             If an exception occurs during the organization export
     * @throws InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since6.0
     */
    String exportOrganization() throws OrganizationExportException;

}
