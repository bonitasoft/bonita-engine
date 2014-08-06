/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package com.bonitasoft.engine.business.application;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public interface ApplicationService {

    String APPLICATION = "APPLICATION";

    String APPLICATION_PAGE = "APPLICATION_PAGE";

    SApplication createApplication(SApplication application) throws SObjectCreationException, SObjectAlreadyExistsException;

    SApplication getApplication(long applicationId) throws SBonitaReadException, SObjectNotFoundException;

    void deleteApplication(long applicationId) throws SObjectModificationException, SObjectNotFoundException;

    long getNumberOfApplications(QueryOptions options) throws SBonitaReadException;

    List<SApplication> searchApplications(QueryOptions options) throws SBonitaSearchException;

    SApplicationPage createApplicationPage(SApplicationPage applicationPage) throws SObjectCreationException, SObjectAlreadyExistsException;

    SApplicationPage getApplicationPage(String applicationName, String applicationPageName) throws SBonitaReadException, SObjectNotFoundException;

    SApplicationPage getApplicationPage(long applicationPageId) throws SBonitaReadException, SObjectNotFoundException;

    void deleteApplicationPage(long applicationpPageId) throws SObjectModificationException, SObjectNotFoundException;

    long getNumberOfApplicationPages(final QueryOptions options) throws SBonitaReadException;

    List<SApplicationPage> searchApplicationPages(final QueryOptions options) throws SBonitaSearchException;
}
