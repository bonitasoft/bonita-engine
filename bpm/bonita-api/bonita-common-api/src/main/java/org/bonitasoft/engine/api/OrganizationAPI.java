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
package org.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.identity.OrganizationExportException;
import org.bonitasoft.engine.identity.OrganizationImportException;

/**
 * Manages the Organization, that is the users, groups, roles, memberships, through import / export methods.
 * 
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public interface OrganizationAPI {

    /**
     * Deletes the organization.
     * <p>
     * It deletes all user memberships, roles, groups, users and custom user info.
     * </p>
     * 
     * @throws DeletionException
     *             If an exception occurs during the organization deletion
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0.0
     */
    void deleteOrganization() throws DeletionException;

    /**
     * Imports the organization using the {@link ImportPolicy#MERGE_DUPLICATES} policy.
     * <p>
     * An organization is composed by users, roles, groups and user memberships.
     * </p>
     *
     * @param organizationContent
     *            the XML content of the organization
     * @throws OrganizationImportException
     *             If an exception occurs during the organization import
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0.0
     */
    void importOrganization(String organizationContent) throws OrganizationImportException;

    /**
     * Imports the organization.
     * <p>
     * An organization is composed by users, roles, groups and user memberships.
     * </p>
     * @param organizationContent
     *            the XML content of the organization
     * @param policy
     *            the import policy
     * @throws OrganizationImportException
     *             If an exception occurs during the organization import
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0.0
     */
    void importOrganization(String organizationContent, ImportPolicy policy) throws OrganizationImportException;

    /**
     * Exports the organization.
     * <p>
     * An organization is composed by users, roles, groups and user memberships.
     * </p>
     *
     * @return the organization contented in an XML format
     * @throws OrganizationExportException
     *             If an exception occurs during the organization export
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0.0
     */
    String exportOrganization() throws OrganizationExportException;

}
