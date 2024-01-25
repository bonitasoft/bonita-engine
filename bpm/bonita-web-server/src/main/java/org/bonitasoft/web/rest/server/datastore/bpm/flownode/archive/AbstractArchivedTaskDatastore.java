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
package org.bonitasoft.web.rest.server.datastore.bpm.flownode.archive;

import org.bonitasoft.engine.bpm.flownode.ArchivedTaskInstance;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedTaskItem;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 */
public abstract class AbstractArchivedTaskDatastore<CONSOLE_ITEM extends ArchivedTaskItem, ENGINE_ITEM extends ArchivedTaskInstance>
        extends AbstractArchivedActivityDatastore<CONSOLE_ITEM, ENGINE_ITEM> {

    public AbstractArchivedTaskDatastore(final APISession engineSession, String token) {
        super(engineSession, token);
    }

    @Override
    protected ENGINE_ITEM runGet(final APIID id) {
        try {
            return super.runGet(id);
        } catch (ClassCastException e) {
            throw new APIItemNotFoundException(this.token, id);
        }
    }

}
