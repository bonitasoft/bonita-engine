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
package org.bonitasoft.web.toolkit.client.common.exception.api;

import org.bonitasoft.web.toolkit.client.common.exception.http.JsonExceptionSerializer;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 */
public class APIItemNotFoundException extends APIItemException {

    private static final long serialVersionUID = -1325943431826471828L;

    private final APIID id;

    public APIItemNotFoundException(final String itemType) {
        this(itemType, null);
    }

    public APIItemNotFoundException(final String itemType, final APIID id) {
        super(itemType);
        this.id = id;
    }

    /**
     * @return the id
     */
    public APIID getId() {
        return this.id;
    }

    @Override
    protected JsonExceptionSerializer buildJson() {
        JsonExceptionSerializer json = super.buildJson();
        if (id != null) {
            json.appendAttribute("id", getId());
        }
        return json;
    }

    @Override
    protected String defaultMessage() {
        StringBuilder message = new StringBuilder();
        message.append(this.itemType.substring(0, 1).toUpperCase());
        message.append(this.itemType.substring(1));
        if (id != null) {
            message.append(" with id (");
            message.append(getId().toString());
            message.append(")");
        }
        message.append(" not found for API ");
        message.append(getApi());
        message.append("#");
        message.append(getResource());
        return message.toString();
    }

}
