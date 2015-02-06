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
package org.bonitasoft.engine.job;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ThrowsExceptionJob implements StatelessJob {

    private static final long serialVersionUID = 3528070481384646426L;

    private boolean throwException = true;

    @Override
    public String getDescription() {
        return "Job that throws a exception";
    }

    @Override
    public void execute() throws SJobExecutionException {
        if (throwException) {
            throw new SJobExecutionException("This job throws an arbitrary exception if parameter 'throwException' is provided.");
        }
    }

    @Override
    public String getName() {
        return "exception";
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) {
        final Boolean result = (Boolean) attributes.get("throwException");
        if (result != null) {
            throwException = result;
        }
    }

}
