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
import java.util.Properties;

import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
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

    /**
     * add a page using the zip in parameters and the given properties
     * 
     * @param page
     * @param content
     * @return
     * @throws SObjectCreationException
     * @throws SObjectAlreadyExistsException
     * @throws SInvalidPageZipException
     * @throws SInvalidPageTokenException
     */
    SPage addPage(SPage page, byte[] content) throws SObjectCreationException, SObjectAlreadyExistsException, SInvalidPageZipException,
            SInvalidPageTokenException;

    SPage getPage(long pageId) throws SBonitaReadException, SObjectNotFoundException;

    SPage getPageByName(String pageName) throws SBonitaReadException;

    /**
     * Read the content of a page in a zip
     *
     * @param content
     *        the page content
     * @return
     *         the properties of the page stored in the page.properties
     * @throws SInvalidPageZipMissingIndexException
     *         if the page is missing an index.html or Index.groovy
     * @throws SInvalidPageZipMissingAPropertyException
     *         if the page is missing mandatory field in the page.properties
     * @throws SInvalidPageZipInconsistentException
     *         if the zip is not a valid zip file or unreadable
     * @throws SInvalidPageZipMissingPropertiesException
     */
    Properties readPageZip(final byte[] content) throws SInvalidPageZipMissingIndexException, SInvalidPageZipMissingAPropertyException,
            SInvalidPageZipInconsistentException, SInvalidPageZipMissingPropertiesException, SInvalidPageTokenException;

    long getNumberOfPages(QueryOptions options) throws SBonitaReadException;

    void deletePage(long pageId) throws SObjectModificationException, SObjectNotFoundException;

    byte[] getPageContent(long pageId) throws SBonitaReadException, SObjectNotFoundException;

    List<SPage> searchPages(QueryOptions options) throws SBonitaReadException;

    SPage updatePage(long pageId, EntityUpdateDescriptor updateDescriptor) throws SObjectModificationException,
            SObjectAlreadyExistsException, SInvalidPageTokenException;

    void updatePageContent(long pageId, byte[] content, String contentName) throws SBonitaException;

    @Override
    void start() throws SBonitaException;

    /**
     * add a page using the zip in parameters, it get all informations from the page.properties file contain inside the zip
     * 
     * @param content
     * @param userId
     * @return
     * @throws SObjectCreationException
     * @throws SObjectAlreadyExistsException
     * @throws SInvalidPageZipException
     * @throws SInvalidPageTokenException
     */
    SPage addPage(final byte[] content, final String contentName, long userId) throws SObjectCreationException, SObjectAlreadyExistsException,
            SInvalidPageZipException,
            SInvalidPageTokenException;

}
