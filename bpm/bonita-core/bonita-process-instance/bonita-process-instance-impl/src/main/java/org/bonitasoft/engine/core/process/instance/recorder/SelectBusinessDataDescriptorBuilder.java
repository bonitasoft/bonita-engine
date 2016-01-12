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
package org.bonitasoft.engine.core.process.instance.recorder;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.process.instance.model.archive.business.data.SARefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public class SelectBusinessDataDescriptorBuilder {

    public static SelectOneDescriptor<SRefBusinessDataInstance> getSRefBusinessDataInstance(final String name, final long processInstanceId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", name);
        parameters.put("processInstanceId", processInstanceId);
        return new SelectOneDescriptor<>("getSRefBusinessDataInstance", parameters, SRefBusinessDataInstance.class);
    }

    public static SelectOneDescriptor<SARefBusinessDataInstance> getSARefBusinessDataInstance(final String name, final long processInstanceId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", name);
        parameters.put("processInstanceId", processInstanceId);
        return new SelectOneDescriptor<>("getSARefBusinessDataInstance", parameters, SARefBusinessDataInstance.class);
    }

    public static SelectListDescriptor<SRefBusinessDataInstance> getSRefBusinessDataInstances(final long processInstanceId, final int startIndex,
            final int maxResults) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("processInstanceId", processInstanceId);
        final QueryOptions options = new QueryOptions(startIndex, maxResults);
        return new SelectListDescriptor<>("getSRefBusinessDataInstancesOfProcess", parameters, SRefBusinessDataInstance.class,
                options);
    }

    public static SelectOneDescriptor<SRefBusinessDataInstance> getSFlowNodeRefBusinessDataInstance(final String name, final long flowNodeInstanceId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", name);
        parameters.put("flowNodeInstanceId", flowNodeInstanceId);
        return new SelectOneDescriptor<>("getSFlowNodeRefBusinessDataInstance", parameters, SRefBusinessDataInstance.class);
    }

    public static SelectOneDescriptor<SARefBusinessDataInstance> getSAFlowNodeRefBusinessDataInstance(final String name, final long flowNodeInstanceId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", name);
        parameters.put("flowNodeInstanceId", flowNodeInstanceId);
        return new SelectOneDescriptor<>("getSAFlowNodeRefBusinessDataInstance", parameters, SARefBusinessDataInstance.class);
    }

    public static SelectOneDescriptor<Integer> getNumberOfDataOfMultiRefBusinessData(final String name, final long processInstanceId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", name);
        parameters.put("processInstanceId", processInstanceId);
        return new SelectOneDescriptor<>("getNumberOfDataOfMultiRefBusinessData", parameters, SMultiRefBusinessDataInstance.class);
    }
}
