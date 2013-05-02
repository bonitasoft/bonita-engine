/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.search.descriptor;

import org.bonitasoft.engine.command.model.SCommandBuilderAccessor;
import org.bonitasoft.engine.core.category.model.builder.SCategoryBuilderAccessor;
import org.bonitasoft.engine.core.process.comment.model.builder.SCommentBuilders;
import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderAccessor;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMappingBuilders;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.profile.builder.SProfileBuilderAccessor;
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogModelBuilder;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilders;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public final class SearchEntitiesDescriptor extends org.bonitasoft.engine.search.SearchEntitiesDescriptor {

    private final SearchLogDescriptor logDescriptor;

    public SearchEntitiesDescriptor(final IdentityModelBuilder identityModelBuilder, final BPMInstanceBuilders bpmInstanceBuilders,
            final FlowNodeStateManager flowNodeStateManager, final SProcessSupervisorBuilders sSupervisorBuilders,
            final BPMDefinitionBuilders definitionBuilders, final SProfileBuilderAccessor sProfileBuilderAccessor, final SCommentBuilders commentBuilders,
            final SCategoryBuilderAccessor categoryBuilderAccessor, final SQueriableLogModelBuilder sQueriableLogModelBuilder,
            final SDocumentMappingBuilderAccessor sDocumentMappingBuilderAccessor, final SExternalIdentityMappingBuilders sExternalIdentityMappingBuilders,
            final SCommandBuilderAccessor commandBuilderAccessor) {
        super(identityModelBuilder, bpmInstanceBuilders, flowNodeStateManager, sSupervisorBuilders, definitionBuilders, commentBuilders,
                categoryBuilderAccessor, sDocumentMappingBuilderAccessor, sExternalIdentityMappingBuilders, commandBuilderAccessor);
        logDescriptor = new SearchLogDescriptor(sQueriableLogModelBuilder.getQueriableLogBuilder());

    }

    public SearchLogDescriptor getLogDescriptor() {
        return logDescriptor;
    }

}
