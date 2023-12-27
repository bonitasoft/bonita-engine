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
package org.bonitasoft.web.rest.server.api.resource;

import java.util.List;

/**
 * Representation for error entity
 *
 * @author Colin Puy
 */
public class ErrorMessageWithExplanations extends ErrorMessage {

    private List<String> explanations;

    // DO NOT PUT stacktrace, this is not coherent with old API toolkit but as a client of REST API, I do not need stacktrace.

    public ErrorMessageWithExplanations() {
        // empty constructor for json serialization
    }

    public ErrorMessageWithExplanations(final Throwable t) {
        super(t);
    }

    public List<String> getExplanations() {
        return explanations;
    }

    public void setExplanations(final List<String> explanations) {
        this.explanations = explanations;
    }
}
