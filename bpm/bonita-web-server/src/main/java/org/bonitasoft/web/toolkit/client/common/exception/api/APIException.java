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

import static org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n.LOCALE;

import org.bonitasoft.web.toolkit.client.common.exception.http.JsonExceptionSerializer;
import org.bonitasoft.web.toolkit.client.common.exception.http.ServerException;
import org.bonitasoft.web.toolkit.client.common.i18n.T_;
import org.bonitasoft.web.toolkit.client.common.json.JsonSerializable;

/**
 * @author Séverin Moussel
 */
public class APIException extends ServerException implements JsonSerializable {

    private static final long serialVersionUID = 1820639344042666872L;

    private LOCALE locale;

    private String api = "...";

    private String resource = "...";

    private T_ localizedMessage;

    protected APIException() {
        super();
    }

    public APIException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public APIException(final String message) {
        super(message);
    }

    public APIException(final Throwable cause) {
        super(cause);
    }

    public APIException(final T_ localizedMessage, final Throwable cause) {
        super(cause);
        this.localizedMessage = localizedMessage;
    }

    public APIException(final T_ localizedMessage) {
        this.localizedMessage = localizedMessage;
    }

    /**
     * @return the api
     */
    public String getApi() {
        return this.api;
    }

    /**
     * @param api
     *        the api to set
     */
    public APIException setApi(final String api) {
        this.api = api;
        return this;
    }

    /**
     * @return the resource
     */
    public String getResource() {
        return this.resource;
    }

    /**
     * @param resource
     *        the resource to set
     */
    public APIException setResource(final String resource) {
        this.resource = resource;
        return this;
    }

    @Override
    protected JsonExceptionSerializer buildJson() {
        return super.buildJson()
                .appendAttribute("api", getApi())
                .appendAttribute("resource", getResource());
    }

    @Override
    protected String defaultMessage() {
        return "The API \"" + getApi() + "#" + getResource() + "\" has encountered an unknown error";
    }

    public void setLocale(LOCALE locale) {
        this.locale = locale;
    }

    @Override
    public String getMessage() {
        if (locale != null && localizedMessage != null) {
            return localizedMessage.localize(locale);
        }
        return super.getMessage();
    }

}
