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
package org.bonitasoft.engine.page;

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
 * @author Laurent Leseigneur
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

    /**
     * get a page attached to a process
     * @param name
     * @param processDefinitionId
     * @return
     * @throws SBonitaReadException
     */
    SPage getPageByNameAndProcessDefinitionId(String name, long processDefinitionId) throws SBonitaReadException;

    /**
     * get a list of page attached to a process
     * @param processDefinitionId
     * @param fromIndex
     * @param numberOfResults
     * @return
     * @throws SBonitaReadException
     */
    List<SPage> getPageByProcessDefinitionId(long processDefinitionId,int fromIndex, int numberOfResults) throws SBonitaReadException;
}
