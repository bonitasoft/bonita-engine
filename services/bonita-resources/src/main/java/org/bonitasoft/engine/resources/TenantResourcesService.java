/**
 * Copyright (C) 2016-2017 Bonitasoft S.A.
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

package org.bonitasoft.engine.resources;

import java.util.List;

import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.SRecorderException;

/**
 * @author Baptiste Mesta
 */
public interface TenantResourcesService {

    String TENANT_RESOURCE = "TENANT_RESOURCE";

    void add(String name, TenantResourceType type, byte[] content, long userId) throws SRecorderException;

    void removeAll(TenantResourceType external) throws SBonitaReadException, SRecorderException;

    List<STenantResource> get(TenantResourceType type, int from, int numberOfElements) throws SBonitaReadException;

    long count(TenantResourceType type) throws SBonitaReadException;

    long count(TenantResourceType type, String name) throws SBonitaReadException;

    STenantResource get(TenantResourceType type, String name) throws SBonitaReadException;

    /**
     * Returns a single STenantResourceLight of the given type. This is the responsibility of the caller to only call
     * this method when he / she is sure that there are not more than one result of the query.
     * If the result is non unique, a SBonitaReadException is thrown.
     * 
     * @param type the type of the resource to filter
     * @return the found resource if unique, null if none found
     * @throws SBonitaReadException if non unique result, or other Hibernate exception is issued
     */
    STenantResourceLight getSingleLightResource(TenantResourceType type) throws SBonitaReadException;

    void remove(STenantResourceLight resource) throws SRecorderException;
}
