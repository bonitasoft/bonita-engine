/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page;

import java.util.List;

import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Baptiste Mesta
 */
public interface PageService extends TenantLifecycleService {

    String PROPERTIES_FILE_NAME = "page.properties";

    String PROPERTIES_DISPLAY_NAME = "displayName";

    String PROPERTIES_DESCRIPTION = "description";

    String PROPERTIES_NAME = "name";

    String PAGE = "PAGE";

    SPage addPage(SPage page, byte[] content) throws SObjectCreationException, SObjectAlreadyExistsException;

    SPage getPage(long pageId) throws SBonitaReadException, SObjectNotFoundException;

    SPage getPageByName(String pageName) throws SBonitaReadException;

    long getNumberOfPages(QueryOptions options) throws SBonitaReadException;

    void deletePage(long pageId) throws SObjectModificationException, SObjectNotFoundException;

    byte[] getPageContent(long pageId) throws SBonitaReadException, SObjectNotFoundException;

    List<SPage> searchPages(QueryOptions options) throws SBonitaSearchException;

    SPage updatePage(long pageId, EntityUpdateDescriptor updateDescriptor) throws SObjectModificationException,
            SObjectAlreadyExistsException;

    void updatePageContent(long pageId, EntityUpdateDescriptor entityUpdateDescriptor) throws SBonitaException;

    @Override
    void start() throws SBonitaException;

}
