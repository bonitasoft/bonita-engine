/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package com.bonitasoft.engine.expression;

import org.bonitasoft.engine.api.impl.APIAccessorImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

import com.bonitasoft.engine.api.impl.APIAccessorExt;

/**
 * @author Matthieu Chaffotte
 */
public class EngineConstantExpressionExecutorStrategy extends org.bonitasoft.engine.expression.EngineConstantExpressionExecutorStrategy {

    /**
     * @param activityInstanceService
     * @param processInstanceService
     * @param sessionService
     * @param sessionAccessor
     */
    public EngineConstantExpressionExecutorStrategy(final ActivityInstanceService activityInstanceService, final ProcessInstanceService processInstanceService,
            final SessionService sessionService, final ReadSessionAccessor sessionAccessor) {
        super(activityInstanceService, processInstanceService, sessionService, sessionAccessor);
    }

    @Override
    protected APIAccessorImpl getApiAccessor() {
        return new APIAccessorExt();
    }

}
