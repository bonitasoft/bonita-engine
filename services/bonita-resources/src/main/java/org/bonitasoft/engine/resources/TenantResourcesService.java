/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

    void add(String name, TenantResourceType type, byte[] content) throws SRecorderException;

    void removeAll(TenantResourceType external) throws SBonitaReadException, SRecorderException;

    List<STenantResource> get(TenantResourceType type, int from, int numberOfElements) throws SBonitaReadException;

    long count(TenantResourceType type) throws SBonitaReadException;

    STenantResource get(TenantResourceType type, String name) throws SBonitaReadException;

    void remove(STenantResourceLight resource) throws SRecorderException;
}
