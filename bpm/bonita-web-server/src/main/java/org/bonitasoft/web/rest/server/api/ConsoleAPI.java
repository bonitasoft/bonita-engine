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
package org.bonitasoft.web.rest.server.api;

import java.io.IOException;

import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.server.framework.API;
import org.bonitasoft.web.toolkit.client.data.item.IItem;

/**
 * @author Séverin Moussel
 */
public abstract class ConsoleAPI<T extends IItem> extends API<T> {

    private APISession sessionSingleton = null;

    /**
     * Get the session to access the engine SDK
     */
    protected APISession getEngineSession() {
        if (this.sessionSingleton == null) {
            this.sessionSingleton = (APISession) getHttpSession().getAttribute("apiSession");
        }
        return this.sessionSingleton;
    }

    /* this method is in visibility Public for testing purpose. */
    @Override
    public String getCompleteTempFilePath(final String path) throws IOException {
        final BonitaHomeFolderAccessor tenantFolder = new BonitaHomeFolderAccessor();
        return tenantFolder.getCompleteTenantTempFilePath(path);
    }
}
