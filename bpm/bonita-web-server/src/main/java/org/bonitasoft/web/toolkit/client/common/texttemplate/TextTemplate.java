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
package org.bonitasoft.web.toolkit.client.common.texttemplate;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text template using %...% syntax <br />
 * The passed parameters will replace %parameter_name% by parameterValue.
 *
 * @author Séverin Moussel
 */
public class TextTemplate {

    private String template = null;

    private List<String> expectedParameters = null;
    private final Pattern regex = Pattern.compile("%(.*?)%");

    public TextTemplate(final String template) {
        this.template = template;
    }

    public String toString(final Map<String, String> data) {
        String result = this.template;

        for (final Entry<String, String> entry : data.entrySet()) {
            result = result.replaceAll("%" + entry.getKey() + "%", entry.getValue() != null ? entry.getValue() : "");
        }

        return result;
    }

    public String toString(final Arg... parameters) {
        String result = this.template;
        for (final Arg parameter : parameters) {
            result = result.replaceAll("%" + parameter.getName() + "%", parameter.getValue());
        }
        return result;
    }

    public String toString(final List<Arg> parameters) {
        String result = this.template;
        for (final Arg parameter : parameters) {
            result = result.replaceAll("%" + parameter.getName() + "%", parameter.getValue());
        }
        return result;
    }

    @Override
    public String toString() {
        return this.template;
    }

    /**
     * Read the template to get the excepted parameters.<br />
     * Expected parameters are the strings between %...%
     */
    public List<String> getExpectedParameters() {
        // Cache result
        if (this.expectedParameters == null) {
            parseExpectedParameters();
        }

        return this.expectedParameters;

    }

    /**
     * Read the template to get the excepted parameters and store the result in the class variable "expectedParameters"
     */
    private void parseExpectedParameters() {
        this.expectedParameters = new LinkedList<>();
        Matcher matcher = regex.matcher(this.template);
        while (matcher.find()) {
            this.expectedParameters.add(matcher.group(1));
        }
    }

}
