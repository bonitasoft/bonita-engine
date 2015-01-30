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
package org.bonitasoft.engine.api.impl.transaction.process;

import java.util.List;

import org.bonitasoft.engine.api.impl.transaction.AbstractGetEntity;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Celine Souchet
 */
public abstract class AbstractGetProcessDeploymentInfo extends AbstractGetEntity<ProcessDeploymentInfo, SProcessDefinitionDeployInfo> {

    public AbstractGetProcessDeploymentInfo(final SearchEntityDescriptor searchDescriptor, final int fromIndex, final int numberOfResults,
            final ProcessDeploymentInfoCriterion criterion) {
        super(searchDescriptor, fromIndex, numberOfResults, criterion.getSort());
    }

    @Override
    public List<ProcessDeploymentInfo> convertToClientObjects(final List<SProcessDefinitionDeployInfo> serverObjects) {
        return ModelConvertor.toProcessDeploymentInfo(serverObjects);
    }

}
