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
package org.bonitasoft.web.rest.model.bpm.process;

import org.bonitasoft.web.rest.model.bpm.AbstractDocumentDefinition;
import org.bonitasoft.web.toolkit.client.data.item.IItem;

/**
 * @author Paul AMAR
 */
public class ProcessDocumentDefinition extends AbstractDocumentDefinition {

    public static final String TOKEN = "process";

    /**
     * the URL of user resource
     */
    private static final String API_URL = "../API/document/process";

    @Override
    protected void definePrimaryKeys() {
    }

    @Override
    protected IItem _createItem() {
        return new ProcessDocumentItem();
    }

    @Override
    protected String defineToken() {
        return TOKEN;
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        // TODO Auto-generated method stub

    }

}
