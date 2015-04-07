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

package org.bonitasoft.engine.core.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.page.URLAdapter;
import org.bonitasoft.engine.page.URLAdapterConstants;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.search.impl.SearchFilter;

/**
 * @author Baptiste Mesta, Anthony Birembaut
 */
public class LegacyURLAdapter implements URLAdapter {

	ProcessDefinitionService processDefinitionService;

	FlowNodeInstanceService flowNodeInstanceService;

	ProcessInstanceService processInstanceService;
	
	FormMappingService formMappingService;

	public LegacyURLAdapter(ProcessDefinitionService processDefinitionService,
			ProcessInstanceService processInstanceService,
			FlowNodeInstanceService flowNodeInstanceService, FormMappingService formMappingService) {
		this.processDefinitionService = processDefinitionService;
		this.processInstanceService = processInstanceService;
		this.flowNodeInstanceService = flowNodeInstanceService;
		this.formMappingService = formMappingService;
	}

	@Override
	public String adapt(String url, String key,
			Map<String, Serializable> context) {
		@SuppressWarnings("unchecked")
		Map<String, String[]> queryParameters = (Map<String, String[]>) context.get(URLAdapterConstants.QUERY_PARAMETERS);
//		QueryOptions queryOptions = new QueryOptions(0, 1);
//        final List<FilterOption> filterOptions = new ArrayList<FilterOption>();
//        filterOptions.add(arg0)
//		formMappingService.searchFormMappings(queryOptions);
//		
//		final StringBuilder legacyFormURL = new StringBuilder(
//				(String) context.get(URLAdapterConstants.CONTEXT_PATH));
//		legacyFormURL
//				.append("/portal/homepage?ui=form&locale=")
//
//				.append((String) context.get(URLAdapterConstants.LOCALE))
//				.append("&theme=")
//				.append(processDefinitionId)
//				.append("#mode=form&form=")
//				.append(URLEncoder.encode(processFormService
//						.getProcessDefinitionUUID(apiSession,
//								processDefinitionId), "UTF-8"));
//		if (taskInstanceId != -1L) {
//			legacyFormURL.append(ProcessFormService.UUID_SEPERATOR)
//					.append(URLEncoder.encode(taskName + "$", "UTF-8"))
//					.append("entry&task=").append(taskInstanceId);
//			if ("true".equals(request.getParameter(ASSIGN_TASK_PARAM))) {
//				legacyFormURL.append("&assignTask=true");
//			}
//		} else if (processInstanceId != -1L) {
//			legacyFormURL.append(URLEncoder.encode("$", "UTF-8"))
//					.append("recap&instance=").append(processInstanceId)
//					.append("&recap=true");
//		} else {
//			legacyFormURL.append(URLEncoder.encode("$", "UTF-8"))
//					.append("entry&process=").append(processDefinitionId)
//					.append("&autoInstantiate=false");
//		}
//		if (userId != -1L) {
//			legacyFormURL.append("&userId=").append(userId);
//		}
//		return legacyFormURL.toString();
		return url;
	}

	@Override
	public String getId() {
		return URLAdapterConstants.LEGACY_URL_ADAPTER;
	}
}
