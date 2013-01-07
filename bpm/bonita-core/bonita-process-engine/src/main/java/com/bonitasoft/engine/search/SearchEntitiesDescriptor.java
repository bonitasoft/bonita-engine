/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.search;

import org.bonitasoft.engine.actor.privilege.model.builder.ActorPrivilegeBuilders;
import org.bonitasoft.engine.command.model.SCommandBuilderAccessor;
import org.bonitasoft.engine.core.category.model.builder.SCategoryBuilderAccessor;
import org.bonitasoft.engine.core.process.comment.model.builder.SCommentBuilders;
import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderAccessor;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMappingBuilders;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.privilege.model.buidler.PrivilegeBuilders;
import org.bonitasoft.engine.profile.model.SProfileBuilderAccessor;
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogModelBuilder;
import org.bonitasoft.engine.supervisor.mapping.model.SSupervisorBuilders;

/**
 * @author Emmanuel Duchastenier
 */
public final class SearchEntitiesDescriptor extends org.bonitasoft.engine.search.SearchEntitiesDescriptor {

    private final SearchLogDescriptor logDescriptor;

    public SearchEntitiesDescriptor(final IdentityModelBuilder identityModelBuilder, final PrivilegeBuilders privilegeBuilders,
            final ActorPrivilegeBuilders actorPrivilegeBuilders, final BPMInstanceBuilders bpmInstanceBuilders,
            final FlowNodeStateManager flowNodeStateManager, final SSupervisorBuilders sSupervisorBuilders, final BPMDefinitionBuilders definitionBuilders,
            final SProfileBuilderAccessor sProfileBuilderAccessor, final SCommentBuilders commentBuilders,
            final SCategoryBuilderAccessor categoryBuilderAccessor, final SQueriableLogModelBuilder sQueriableLogModelBuilder,
            final SDocumentMappingBuilderAccessor sDocumentMappingBuilderAccessor, final SExternalIdentityMappingBuilders sExternalIdentityMappingBuilders,
            final SCommandBuilderAccessor commandBuilderAccessor) {
        super(identityModelBuilder, privilegeBuilders, actorPrivilegeBuilders, bpmInstanceBuilders, flowNodeStateManager, sSupervisorBuilders,
                definitionBuilders, sProfileBuilderAccessor, commentBuilders, categoryBuilderAccessor, sDocumentMappingBuilderAccessor,
                sExternalIdentityMappingBuilders, commandBuilderAccessor);
        logDescriptor = new SearchLogDescriptor(sQueriableLogModelBuilder.getQueriableLogBuilder());

    }

    public SearchLogDescriptor getLogDescriptor() {
        return logDescriptor;
    }
}
