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
package org.bonitasoft.engine.execution.state;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class AbortingCallActivityStateImpl extends EndingCallActivityExceptionStateImpl {

    public AbortingCallActivityStateImpl(final ActivityInstanceService activityInstanceService, final ProcessInstanceService processInstanceService,
            final ContainerRegistry containerRegistry, final ArchiveService archiveService, final SCommentService commentService,
            final DataInstanceService dataInstanceService, final DocumentService documentService, final TechnicalLoggerService logger,
                                         final ProcessDefinitionService processDefinitionService, final ConnectorInstanceService connectorInstanceService, ClassLoaderService classLoaderService) {
        super(activityInstanceService, processInstanceService, containerRegistry, archiveService, commentService, dataInstanceService, documentService,
                logger, processDefinitionService, connectorInstanceService, classLoaderService);
    }

    @Override
    public int getId() {
        return 20;
    }

    @Override
    public String getName() {
        return "aborting";
    }

    @Override
    public SStateCategory getStateCategory() {
        return SStateCategory.ABORTING;
    }

    @Override
    public boolean mustAddSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return false;
    }

    @Override
    public String getSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return "aborting call activity";
    }

}
