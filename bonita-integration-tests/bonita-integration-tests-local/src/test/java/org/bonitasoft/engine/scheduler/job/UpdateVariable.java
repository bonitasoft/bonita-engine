/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Matthieu Chaffotte
 */
public class UpdateVariable extends GroupJob {

    private static final long serialVersionUID = 8379781766551862114L;

    private String variableName;

    private Object variableValue;

    public UpdateVariable(final String variableName, final Object variableValue) {
        super();
        this.variableName = variableName;
        this.variableValue = variableValue;
    }

    @Override
    public void execute() {
        final VariableStorage storage = VariableStorage.getInstance();
        storage.setVariable(variableName, variableValue);
    }

    @Override
    public String getDescription() {
        return "Change the value of " + variableName + " with " + variableValue;
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) {
        super.setAttributes(attributes);
        variableName = (String) attributes.get("variableName");
        variableValue = attributes.get("variableValue");
    }

}
