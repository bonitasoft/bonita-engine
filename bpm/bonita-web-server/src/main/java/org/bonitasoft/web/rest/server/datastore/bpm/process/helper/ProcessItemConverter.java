/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.rest.server.datastore.bpm.process.helper;

import org.bonitasoft.console.common.server.utils.TenantCacheUtil;
import org.bonitasoft.console.common.server.utils.TenantCacheUtilFactory;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.actor.ActorNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.rest.server.datastore.converter.ItemConverter;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;
import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;

/**
 * @author Vincent Elcrin
 */
public class ProcessItemConverter extends ItemConverter<ProcessItem, ProcessDeploymentInfo> {

    private final ProcessAPI processApi;

    public ProcessItemConverter(final ProcessAPI processApi) {
        this.processApi = processApi;
    }

    @Override
    public ProcessItem convert(ProcessDeploymentInfo engineItem) {

        final ProcessItem item = new ProcessItem();
        item.setId(engineItem.getProcessId());
        item.setName(engineItem.getName());
        item.setVersion(engineItem.getVersion());
        item.setDescription(engineItem.getDescription());
        item.setDeployedByUserId(engineItem.getDeployedBy());
        item.setDeploymentDate(engineItem.getDeploymentDate());
        item.setActivationState(engineItem.getActivationState().name());
        item.setConfigurationState(engineItem.getConfigurationState().name());
        item.setDisplayName(engineItem.getDisplayName());
        item.setDisplayDescription(engineItem.getDisplayDescription());
        item.setLastUpdateDate(engineItem.getLastUpdateDate());
        item.setActorInitiatorId(getActorInitiator(engineItem));

        return item;
    }

    private Long getActorInitiator(ProcessDeploymentInfo engineItem) {
        TenantCacheUtil tenantCacheUtil = TenantCacheUtilFactory.getTenantCacheUtil();
        Long actorInitiatorId = tenantCacheUtil.getProcessActorInitiatorId(engineItem.getProcessId());
        if (actorInitiatorId == null) {
            actorInitiatorId = tenantCacheUtil.storeProcessActorInitiatorId(engineItem.getProcessId(),
                    getActorInitiatorFromEngine(engineItem));
        }
        return actorInitiatorId;
    }

    private Long getActorInitiatorFromEngine(ProcessDeploymentInfo engineItem) {
        Long actorInitiatorId;
        try {
            actorInitiatorId = processApi.getActorInitiator(engineItem.getProcessId()).getId();
        } catch (ActorNotFoundException e) {
            actorInitiatorId = -1L;
        } catch (ProcessDefinitionNotFoundException e) {
            throw new APIException(AbstractI18n.t_("Process definition not found for id %processId%",
                    new Arg("processId", String.valueOf(engineItem.getProcessId()))), e);
        }
        return actorInitiatorId;
    }

}
