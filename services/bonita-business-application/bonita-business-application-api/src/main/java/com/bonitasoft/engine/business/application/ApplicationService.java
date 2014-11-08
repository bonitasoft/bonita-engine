/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;
import com.bonitasoft.engine.business.application.model.SApplicationPage;

/**
 * @author Elias Ricken de Medeiros
 */
public interface ApplicationService {

    String APPLICATION = "APPLICATION";

    String APPLICATION_PAGE = "APPLICATION_PAGE";

    String APPLICATION_MENU = "APPLICATION_MENU";

    SApplication createApplication(SApplication application) throws SObjectCreationException, SObjectAlreadyExistsException, SInvalidTokenException,
            SInvalidDisplayNameException;

    SApplication getApplication(long applicationId) throws SBonitaReadException, SObjectNotFoundException;

    SApplication getApplicationByToken(String token) throws SBonitaReadException;

    void deleteApplication(long applicationId) throws SObjectModificationException, SObjectNotFoundException;

    SApplication updateApplication(long applicationId, EntityUpdateDescriptor updateDescriptor) throws SObjectModificationException, SInvalidTokenException,
            SInvalidDisplayNameException, SBonitaReadException, SObjectAlreadyExistsException, SObjectNotFoundException;

    SApplication updateApplication(SApplication application, EntityUpdateDescriptor updateDescriptor) throws SObjectModificationException,
            SInvalidTokenException,
            SInvalidDisplayNameException, SBonitaReadException, SObjectAlreadyExistsException;

    long getNumberOfApplications(QueryOptions options) throws SBonitaReadException;

    List<SApplication> searchApplications(QueryOptions options) throws SBonitaReadException;

    SApplicationPage createApplicationPage(SApplicationPage applicationPage) throws SObjectCreationException, SObjectAlreadyExistsException,
            SInvalidTokenException;

    SApplicationPage getApplicationPage(String applicationName, String applicationPageToken) throws SBonitaReadException, SObjectNotFoundException;

    SApplicationPage getApplicationPage(long applicationPageId) throws SBonitaReadException, SObjectNotFoundException;

    SApplicationPage getApplicationHomePage(long applicationId) throws SBonitaReadException, SObjectNotFoundException;

    SApplicationPage deleteApplicationPage(long applicationPageId) throws SObjectModificationException, SObjectNotFoundException;

    void deleteApplicationPage(SApplicationPage applicationPage) throws SObjectModificationException;

    long getNumberOfApplicationPages(final QueryOptions options) throws SBonitaReadException;

    List<SApplicationPage> searchApplicationPages(final QueryOptions options) throws SBonitaReadException;

    SApplicationMenu createApplicationMenu(SApplicationMenu applicationMenu) throws SObjectCreationException;

    SApplicationMenu updateApplicationMenu(long applicationMenuId, EntityUpdateDescriptor updateDescriptor) throws SObjectModificationException,
            SObjectNotFoundException;

    SApplicationMenu updateApplicationMenu(SApplicationMenu applicationMenu, EntityUpdateDescriptor updateDescriptor, boolean organizeIndexes)
            throws SObjectModificationException;

    SApplicationMenu getApplicationMenu(long applicationMenuId) throws SBonitaReadException, SObjectNotFoundException;

    SApplicationMenu deleteApplicationMenu(long applicationMenuId) throws SObjectModificationException, SObjectNotFoundException;

    public void deleteApplicationMenu(SApplicationMenu applicationMenu) throws SObjectModificationException;

    long getNumberOfApplicationMenus(QueryOptions options) throws SBonitaReadException;

    List<SApplicationMenu> searchApplicationMenus(QueryOptions options) throws SBonitaReadException;

    List<String> getAllPagesForProfile(long profileId) throws SBonitaReadException;

    int getNextAvailableIndex(Long parentMenuId) throws SBonitaReadException;

    int getLastUsedIndex(Long parentMenuId) throws SBonitaReadException;

}
