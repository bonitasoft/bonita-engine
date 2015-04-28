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

import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;

/**
 * @author Matthieu Chaffotte
 */
public class IncrementVariableJob extends GroupJob {

    private static final long serialVersionUID = 3707724945060118636L;

    private String variableName;

    private int throwExceptionAfterNIncrements;

    @Override
    public void execute() throws SJobExecutionException {
        synchronized (IncrementVariableJob.class) {

            final VariableStorage storage = VariableStorage.getInstance();
            final Integer value = (Integer) storage.getVariableValue(variableName);
            if (value == null) {
                storage.setVariable(variableName, 1);
            } else if (value + 1 == throwExceptionAfterNIncrements) {
                throw new SJobExecutionException("Increment reached");
            } else {
                storage.setVariable(variableName, value + 1);
            }
        }
    }

    @Override
    public String getDescription() {
        return "Increment the variable " + variableName;
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) {
        super.setAttributes(attributes);
        variableName = (String) attributes.get("variableName");
        throwExceptionAfterNIncrements = (Integer) attributes.get("throwExceptionAfterNIncrements");
    }

}
