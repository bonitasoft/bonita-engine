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
package org.bonitasoft.web.rest.server.datastore;

import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;

/**
 * @author Celine Souchet
 */
public class FakeCommonDatastore extends CommonDatastore<ProcessItem, ProcessDefinition> {

    public FakeCommonDatastore(final APISession engineSession) {
        super(engineSession);
    }

    @Override
    protected ProcessItem convertEngineToConsoleItem(final ProcessDefinition item) {
        return null;
    }

}
