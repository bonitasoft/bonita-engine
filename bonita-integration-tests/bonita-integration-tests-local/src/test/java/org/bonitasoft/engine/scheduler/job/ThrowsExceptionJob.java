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

import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ThrowsExceptionJob implements StatelessJob {

    private static final long serialVersionUID = 1L;

    public static final String THROW_EXCEPTION = "throwException";

    private Boolean throwException = null;

    @Override
    public String getDescription() {
        return "Job that throws an exception";
    }

    @Override
    public void execute() throws SJobExecutionException {
        if (throwException != null && throwException) {
            throw new SJobExecutionException("This job throws an arbitrary exception");
        }
    }

    @Override
    public String getName() {
        return "ThrowsExceptionJob";
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) {
        final Boolean result = (Boolean) attributes.get(THROW_EXCEPTION);
        if (result != null) {
            throwException = result;
        }
    }

}
