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
package org.bonitasoft.engine.supervisor.mapping.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilderFactory;

/**
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SelectDescriptorBuilder {

    public static SelectByIdDescriptor<SProcessSupervisor> getSupervisor(final long supervisorId) {
        return new SelectByIdDescriptor<SProcessSupervisor>("getSupervisorById", SProcessSupervisor.class, supervisorId);
    }

    public static SelectOneDescriptor<Long> getNumberOfSupervisors(final long processDefId) {
        final Map<String, Object> parameters = Collections.singletonMap("processDefId", (Object) processDefId);
        return new SelectOneDescriptor<Long>("getNumberOfSupervisorsOfProcessDef", parameters, SProcessSupervisor.class);
    }

    public static SelectOneDescriptor<SProcessSupervisor> getSupervisor(final long processDefId, final long userId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("processDefId", processDefId);
        parameters.put("userId", userId);
        return new SelectOneDescriptor<SProcessSupervisor>("getSupervisor", parameters, SProcessSupervisor.class);
    }

    public static SelectListDescriptor<Long> getProcessDefIdsOfUser(final long userId, final int fromIndex, final int maxResult, final OrderByType orderByType) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userId", userId);

        final OrderByOption orderByOption = new OrderByOption(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getProcessDefIdKey(),
                orderByType);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResult, Collections.singletonList(orderByOption));

        return new SelectListDescriptor<Long>("getProcessDefIdsOfUser", parameters, SProcessSupervisor.class, queryOptions);
    }

}
