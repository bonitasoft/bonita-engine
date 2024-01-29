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
package org.bonitasoft.web.toolkit.client.data.item.attribute;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;
import org.bonitasoft.web.toolkit.client.common.texttemplate.TextTemplate;

/**
 * @author Séverin Moussel
 */
public class ValidationError {

    private final String attributeName;

    private final TextTemplate template;

    public ValidationError(final String attributeName, final String template) {
        this.attributeName = attributeName;
        this.template = new TextTemplate(template);
    }

    public String getMessage() {
        final List<Arg> args = new ArrayList<>();
        for (final String parameterName : this.template.getExpectedParameters()) {
            args.add(new Arg(parameterName, parameterName));
        }

        return this.template.toString(args);
    }

    @Override
    public String toString() {
        return this.getMessage();
    }

}
