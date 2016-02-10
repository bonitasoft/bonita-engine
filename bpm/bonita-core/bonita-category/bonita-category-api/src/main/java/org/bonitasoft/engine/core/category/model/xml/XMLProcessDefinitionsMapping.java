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
package org.bonitasoft.engine.core.category.model.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yanyan Liu
 */
public class XMLProcessDefinitionsMapping {

    private List<String> ids;

    public XMLProcessDefinitionsMapping(final List<String> processDefinitionIds) {
        this.ids = processDefinitionIds;
    }

    public XMLProcessDefinitionsMapping() {
        this.ids = new ArrayList<String>();
    }

    public List<String> getIds() {
        return this.ids;
    }

    public void addId(final String id) {
        if (this.ids == null) {
            this.ids = new ArrayList<String>();
        }
        this.ids.add(id);
    }

    public boolean deleteId(final String id) {
        return this.ids.remove(id);
    }

    public void deleteAll() {
        this.ids.clear();
    }

    public boolean contains(final String processDefinitionId) {
        return this.ids.contains(processDefinitionId);
    }
}
