/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
public interface ProcessResourcesService {

    String BAR_RESOURCE = "BAR_RESOURCE";

    void add(long processDefinitionId, String name, BARResourceType type, byte[] content) throws SRecorderException;

    void update(SBARResourceLight resource, byte[] content) throws SRecorderException;

    void removeAll(long processDefinitionId, BARResourceType external) throws SBonitaReadException, SRecorderException;

    List<SBARResource> get(long processDefinitionId, BARResourceType type, int from, int numberOfElements) throws SBonitaReadException;

    long count(long processDefinitionId, BARResourceType type) throws SBonitaReadException;

    SBARResource get(long processDefinitionId, BARResourceType type, String name) throws SBonitaReadException;

    void remove(SBARResourceLight resource) throws SRecorderException;
}
