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
package org.bonitasoft.engine.core.form;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Baptiste Mesta
 */
public interface FormMappingService {

    SFormMapping create(long processDefinitionId, String task, Integer type, String target, String form)
            throws SObjectCreationException, SBonitaReadException;

    void update(SFormMapping formMapping, String url, Long pageId) throws SObjectModificationException;

    void delete(SFormMapping formMapping) throws SObjectModificationException;

    SFormMapping get(long formMappingId) throws SBonitaReadException, SObjectNotFoundException;

    SFormMapping get(String key) throws SBonitaReadException, SObjectNotFoundException;

    SFormMapping get(long processDefinitionId, Integer type, String task) throws SBonitaReadException, SObjectNotFoundException;

    SFormMapping get(long processDefinitionId, Integer type) throws SBonitaReadException, SObjectNotFoundException;

    List<SFormMapping> list(long processDefinitionId, int fromIndex, int numberOfResults) throws SBonitaReadException;

    List<SFormMapping> list(int fromIndex, int numberOfResults) throws SBonitaReadException;

    List<SFormMapping> searchFormMappings(QueryOptions queryOptions) throws SBonitaReadException;

    long getNumberOfFormMappings(QueryOptions queryOptions) throws SBonitaReadException;
}
