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

import java.util.List;

import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.identity.OrganizationExportException;
import org.bonitasoft.engine.identity.OrganizationImportException;
import org.bonitasoft.engine.identity.UserUpdater;

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
     * <p>Use this method with caution: some artifacts like {@link Application}s and {@link DesignProcessDefinition}s may present display problems in the Bonita
     * BPM Portal if the referenced user was deleted. Note that you can disable a user instead of deleting it. To do so, use the method
     * {@link IdentityAPI#updateUser(long, UserUpdater)} to set the attribute 'enabled' to false</p>.
     *
     * @throws DeletionException
     *         If an exception occurs during the organization deletion
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0.0
     * @see IdentityAPI#updateUser(long, UserUpdater)
     * @see Application
     * @see DesignProcessDefinition
     */
    void deleteOrganization() throws DeletionException;

    /**
     * Imports the organization using the {@link ImportPolicy#MERGE_DUPLICATES} policy.
     * <p>
     * An organization is composed by users, roles, groups and user memberships.
     * </p>
     *
     * @param organizationContent
     *        the XML content of the organization
     * @throws OrganizationImportException
     *         If an exception occurs during the organization import
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0.0
     */
    void importOrganization(String organizationContent) throws OrganizationImportException;

    /**
     * Imports the organization.
     * <p>
     * An organization is composed by users, roles, groups and user memberships.
     * </p>
     * 
     * @param organizationContent
     *        the XML content of the organization
     * @param policy
     *        the import policy
     * @throws OrganizationImportException
     *         If an exception occurs during the organization import
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0.0
     *
     * @deprecated since 7.5.2, use {@link OrganizationAPI#importOrganizationWithWarnings(String, ImportPolicy)} instead.
     *
     */
    @Deprecated
    void importOrganization(String organizationContent, ImportPolicy policy) throws OrganizationImportException;


    /**
     * Imports the organization. Returns the error/info messages about what occurred during the process.
     * Functionally the same thing that {@link OrganizationAPI#importOrganization(String, ImportPolicy)} but allows
     * to inform the end-user of particular non-critical events that occurred during the import.
     * <p>
     * Ex: If a group in the organization contains an illegal character in their name, the method will import all the other
     * groups in the organization, and return a List with one String : "The group name (...) contains the illegal character (...). The group has not been imported"
     * </p>
     * <p>
     * An organization is composed by users, roles, groups and user memberships.
     * </p>
     *
     * @return List of warning messages
     * @param organizationContent
     *        the XML content of the organization
     * @param policy
     *        the import policy
     * @throws OrganizationImportException
     *         If an exception occurs during the organization import
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 7.5.2 Replaces {@link OrganizationAPI#importOrganizationWithWarnings(String, ImportPolicy)}
     */
    List<String> importOrganizationWithWarnings(String organizationContent, ImportPolicy policy) throws OrganizationImportException;

    /**
     * Exports the organization.
     * <p>
     * An organization is composed by users, roles, groups and user memberships.
     * </p>
     *
     * @return the organization contented in an XML format
     * @throws OrganizationExportException
     *         If an exception occurs during the organization export
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0.0
     */
    String exportOrganization() throws OrganizationExportException;

}
