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
package org.bonitasoft.engine.core.process.instance.api;

import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.business.data.SMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Matthieu Chaffotte
 */
public interface RefBusinessDataService {

    String NEW_REF_BUISNESS_DATA_INSTANCE_ADDED = "New reference to a business data added";

    String REF_BUSINESS_DATA_INSTANCE = "REF_BUSINESS_DATA_INSTANCE";

    SRefBusinessDataInstance getRefBusinessDataInstance(String name, long processInstanceId)
            throws SRefBusinessDataInstanceNotFoundException, SBonitaReadException;

    List<SRefBusinessDataInstance> getRefBusinessDataInstances(long processInstanceId, int startIndex, int maxResults)
            throws SBonitaReadException;

    SRefBusinessDataInstance getFlowNodeRefBusinessDataInstance(String name, long flowNodeInstanceId)
            throws SRefBusinessDataInstanceNotFoundException, SBonitaReadException;

    SRefBusinessDataInstance addRefBusinessDataInstance(SRefBusinessDataInstance instance) throws SRefBusinessDataInstanceCreationException;

    void updateRefBusinessDataInstance(SSimpleRefBusinessDataInstance refBusinessDataInstance, Long dataId)
            throws SRefBusinessDataInstanceModificationException;

    void updateRefBusinessDataInstance(SMultiRefBusinessDataInstance refBusinessDataInstance, List<Long> dataIds)
            throws SRefBusinessDataInstanceModificationException;

    int getNumberOfDataOfMultiRefBusinessData(String name, long processInstanceId) throws SBonitaReadException;

}
