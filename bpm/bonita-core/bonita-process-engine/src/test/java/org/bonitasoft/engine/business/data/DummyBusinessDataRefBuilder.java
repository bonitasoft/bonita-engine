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

package org.bonitasoft.engine.business.data;

import java.util.List;

import org.bonitasoft.engine.core.process.instance.model.archive.business.data.SASimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.business.data.SAProcessSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.business.data.SASimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SProcessMultiRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SProcessSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SSimpleRefBusinessDataInstanceImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class DummyBusinessDataRefBuilder {

    public static SSimpleRefBusinessDataInstance buildSimpleRefBusinessData(final Long dataId) {
        final SSimpleRefBusinessDataInstanceImpl sRefBusinessDataInstanceImpl = new SProcessSimpleRefBusinessDataInstanceImpl();
        sRefBusinessDataInstanceImpl.setDataId(dataId);
        return sRefBusinessDataInstanceImpl;
    }

    public static SSimpleRefBusinessDataInstance buildSimpleRefBusinessData(final String dataName, final String dataClassName) {
        final SSimpleRefBusinessDataInstanceImpl sRefBusinessDataInstanceImpl = new SProcessSimpleRefBusinessDataInstanceImpl();
        sRefBusinessDataInstanceImpl.setName(dataName);
        sRefBusinessDataInstanceImpl.setDataClassName(dataClassName);
        return sRefBusinessDataInstanceImpl;
    }

    public static SProcessMultiRefBusinessDataInstance buildMultiRefBusinessData(final List<Long> dataIds) {
        final SProcessMultiRefBusinessDataInstanceImpl reference = new SProcessMultiRefBusinessDataInstanceImpl();
        reference.setDataIds(dataIds);
        return reference;
    }

    public static SProcessMultiRefBusinessDataInstance buildMultiRefBusinessData(final String dataClassName) {
        final SProcessMultiRefBusinessDataInstanceImpl reference = new SProcessMultiRefBusinessDataInstanceImpl();
        reference.setDataClassName(dataClassName);
        return reference;
    }

    public static SASimpleRefBusinessDataInstance buildArchivedSimpleRefBusinessData(final Long dataId) {
        final SASimpleRefBusinessDataInstanceImpl saProcessSimpleRefBusinessDataInstance = new SAProcessSimpleRefBusinessDataInstanceImpl();
        saProcessSimpleRefBusinessDataInstance.setDataId(dataId);
        return saProcessSimpleRefBusinessDataInstance;
    }

}
