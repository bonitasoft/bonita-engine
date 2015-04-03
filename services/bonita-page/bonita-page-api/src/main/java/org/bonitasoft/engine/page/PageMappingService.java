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
 */

package org.bonitasoft.engine.page;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SDeletionException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Baptiste Mesta
 */
public interface PageMappingService {

    void addUrlAdapter(URLAdapter urlAdapter);

    /**
     * @param key the key used to retrieve the mapping
     * @param pageId the id of the custom page
     * @return the created page mapping
     * @throws SObjectCreationException when there is an issue while creating this object
     * @since 7.0.0
     */
    SPageMapping create(String key, Long pageId) throws SObjectCreationException;

    /**
     * @param key the key used to retrieve the mapping
     * @param url the external url the mapping points to
     * @param urlAdapter the name of the url adapter that transform the url in case of an external url. i.e. it can add parameters
     * @return the created page mapping
     * @throws SObjectCreationException when there is an issue while creating this object
     * @since 7.0.0
     */
    SPageMapping create(String key, String url, String urlAdapter) throws SObjectCreationException;

    /**
     * @param key the key of the page mapping to retrieve
     * @return the page mapping having this key
     * @throws SObjectNotFoundException when there is no mapping having this key
     */
    SPageMapping get(String key) throws SObjectNotFoundException, SBonitaReadException;

    SPageURL resolvePageURL(SPageMapping pageMapping, Map<String, Serializable> context) throws SExecutionException;

    /**
     * delete this page mapping
     *
     * @param SPageMapping the page mapping to delete
     */
    void delete(SPageMapping SPageMapping) throws SDeletionException;

    /**
     * update the given page mapping
     *
     * @param pageMapping the pageMapping to update
     * @param pageId the id of the page or null
     * @throws SObjectModificationException
     */
    void update(SPageMapping pageMapping, Long pageId) throws SObjectModificationException, SObjectNotFoundException, SBonitaReadException;

    /**
     * update the given page mapping
     *
     * @param pageMapping the pageMapping to update
     * @param url the url or null
     * @param urlAdapter the new url adapter to use
     * @throws SObjectModificationException
     */
    void update(SPageMapping pageMapping, String url, String urlAdapter) throws SObjectModificationException, SObjectNotFoundException, SBonitaReadException;
}
