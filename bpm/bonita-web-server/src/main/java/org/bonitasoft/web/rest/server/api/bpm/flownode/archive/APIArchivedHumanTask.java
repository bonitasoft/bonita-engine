/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.bpm.flownode.archive;

import java.util.List;

import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedHumanTaskDefinition;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedHumanTaskItem;
import org.bonitasoft.web.rest.server.api.bpm.flownode.AbstractAPIHumanTask;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.archive.ArchivedHumanTaskDatastore;

/**
 * @author Séverin Moussel
 */
public class APIArchivedHumanTask extends AbstractAPIHumanTask<ArchivedHumanTaskItem> {

    @Override
    protected ArchivedHumanTaskDefinition defineItemDefinition() {
        return new ArchivedHumanTaskDefinition();
    }

    @Override
    protected ArchivedHumanTaskDatastore defineDefaultDatastore() {
        return new ArchivedHumanTaskDatastore(getEngineSession(), ArchivedHumanTaskDefinition.TOKEN);
    }

    @Override
    protected List<String> defineReadOnlyAttributes() {
        final List<String> attributes = super.defineReadOnlyAttributes();

        attributes.add(ArchivedHumanTaskItem.ATTRIBUTE_ARCHIVED_DATE);

        return attributes;
    }

}
