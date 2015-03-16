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
package org.bonitasoft.engine.business.data;

import java.io.IOException;
import java.util.List;

import org.bonitasoft.engine.bdm.Entity;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface JsonBusinessDataSerializer {

    String EMPTY_OBJECT = "{}";

    String serializeEntity(Entity entity, String businessDataURIPattern) throws JsonGenerationException, JsonMappingException, IOException;

    String serializeEntity(List<Entity> childEntity, String businessDataURIPattern) throws JsonGenerationException, JsonMappingException, IOException;

}
