/**
 * Copyright (C) 2011, 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.scheduler.builder.impl;

import org.bonitasoft.engine.scheduler.builder.SJobLogQueriableLogBuilder;
import org.bonitasoft.engine.scheduler.builder.SJobParameterQueriableLogBuilder;
import org.bonitasoft.engine.scheduler.builder.SJobQueriableLogBuilder;
import org.bonitasoft.engine.scheduler.builder.SSchedulerBuilderAccessor;
import org.bonitasoft.engine.scheduler.builder.SSchedulerQueriableLogBuilder;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SSchedulerBuilderAccessorImpl implements SSchedulerBuilderAccessor {

    @Override
    public SJobQueriableLogBuilder getSJobQueriableLogBuilder() {
        return new SJobQueriableLogBuilderImpl();
    }

    @Override
    public SSchedulerQueriableLogBuilder getSSchedulerQueriableLogBuilder() {
        return new SSchedulerQueriableLogBuilderImpl();
    }

    @Override
    public SJobParameterQueriableLogBuilder getSJobParameterQueriableLogBuilder() {
        return new SJobParameterQueriableLogBuilderImpl();
    }

    @Override
    public SJobLogQueriableLogBuilder getSJobLogQueriableLogBuilder() {
        return new SJobLogQueriableLogBuilderImpl();
    }

}
