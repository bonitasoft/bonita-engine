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
package org.bonitasoft.engine.business.application;

import java.util.List;

import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.*;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 */
public interface ApplicationService extends TenantLifecycleService {

    String APPLICATION = "APPLICATION";

    String APPLICATION_PAGE = "APPLICATION_PAGE";

    String APPLICATION_MENU = "APPLICATION_MENU";

    String DEFAULT_LAYOUT_NAME = "custompage_layoutBonita";

    String DEFAULT_THEME_NAME = "custompage_themeBonita";

    SApplicationWithIcon createApplication(SApplicationWithIcon application)
            throws SObjectCreationException, SObjectAlreadyExistsException;

    SApplication getApplication(long applicationId) throws SBonitaReadException, SObjectNotFoundException;

    SApplicationWithIcon getApplicationWithIcon(long applicationId)
            throws SBonitaReadException, SObjectNotFoundException;

    SApplication getApplicationByToken(String token) throws SBonitaReadException;

    void deleteApplication(long applicationId) throws SObjectModificationException, SObjectNotFoundException;

    void forceDeleteApplication(SApplication application) throws SObjectModificationException;

    SApplicationWithIcon updateApplication(long applicationId, EntityUpdateDescriptor updateDescriptor)
            throws SObjectModificationException,
            SObjectAlreadyExistsException, SObjectNotFoundException;

    SApplicationWithIcon updateApplication(SApplicationWithIcon application,
            EntityUpdateDescriptor updateDescriptor)
            throws SObjectModificationException,
            SObjectAlreadyExistsException;

    long getNumberOfApplications(QueryOptions options) throws SBonitaReadException;

    List<SApplication> searchApplications(QueryOptions options) throws SBonitaReadException;

    SApplicationPage createApplicationPage(SApplicationPage applicationPage)
            throws SObjectCreationException, SObjectAlreadyExistsException;

    SApplicationPage getApplicationPage(String applicationName, String applicationPageToken)
            throws SBonitaReadException, SObjectNotFoundException;

    SApplicationPage getApplicationPage(long applicationPageId) throws SBonitaReadException, SObjectNotFoundException;

    SApplicationPage getApplicationHomePage(long applicationId) throws SBonitaReadException, SObjectNotFoundException;

    SApplicationPage deleteApplicationPage(long applicationPageId)
            throws SObjectModificationException, SObjectNotFoundException;

    void deleteApplicationPage(SApplicationPage applicationPage) throws SObjectModificationException;

    long getNumberOfApplicationPages(final QueryOptions options) throws SBonitaReadException;

    List<SApplicationPage> searchApplicationPages(final QueryOptions options) throws SBonitaReadException;

    SApplicationMenu createApplicationMenu(SApplicationMenu applicationMenu) throws SObjectCreationException;

    SApplicationMenu updateApplicationMenu(long applicationMenuId, EntityUpdateDescriptor updateDescriptor)
            throws SObjectModificationException,
            SObjectNotFoundException;

    SApplicationMenu updateApplicationMenu(SApplicationMenu applicationMenu, EntityUpdateDescriptor updateDescriptor,
            boolean organizeIndexes)
            throws SObjectModificationException;

    SApplicationMenu getApplicationMenu(long applicationMenuId) throws SBonitaReadException, SObjectNotFoundException;

    SApplicationMenu deleteApplicationMenu(long applicationMenuId)
            throws SObjectModificationException, SObjectNotFoundException;

    void deleteApplicationMenu(SApplicationMenu applicationMenu) throws SObjectModificationException;

    long getNumberOfApplicationMenus(QueryOptions options) throws SBonitaReadException;

    List<SApplicationMenu> searchApplicationMenus(QueryOptions options) throws SBonitaReadException;

    List<String> getAllPagesForProfile(long profileId) throws SBonitaReadException;

    List<String> getAllPagesForProfile(String profile) throws SBonitaReadException;

    int getNextAvailableIndex(Long parentMenuId) throws SBonitaReadException;

    int getLastUsedIndex(Long parentMenuId) throws SBonitaReadException;

    long getNumberOfApplicationsOfUser(long userId, QueryOptions options) throws SBonitaReadException;

    List<SApplication> searchApplicationsOfUser(long userId, QueryOptions options) throws SBonitaReadException;
}
