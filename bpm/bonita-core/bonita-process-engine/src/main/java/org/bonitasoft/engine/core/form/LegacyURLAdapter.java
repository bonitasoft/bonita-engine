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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.page.URLAdapter;
import org.bonitasoft.engine.page.URLAdapterConstants;

/**
 * @author Baptiste Mesta, Anthony Birembaut
 */
public class LegacyURLAdapter implements URLAdapter {
    
    private static final String UUID_SEPERATOR = "--";
    
    private static final String DEFAULT_FORM_MODE = "form";

    ProcessDefinitionService processDefinitionService;
    
    FormMappingService formMappingService;

    public LegacyURLAdapter(final ProcessDefinitionService processDefinitionService, final FormMappingService formMappingService) {
        this.processDefinitionService = processDefinitionService;
        this.formMappingService = formMappingService;
    }

    @Override
    public String adapt(final String url, final String key, final Map<String, Serializable> context) throws SExecutionException {
        @SuppressWarnings("unchecked")
        final Map<String, String[]> queryParameters = (Map<String, String[]>) context.get(URLAdapterConstants.QUERY_PARAMETERS);

        String[] idParamValue = new String[0];
        if(queryParameters != null){
            idParamValue = queryParameters.get(URLAdapterConstants.ID_QUERY_PARAM);
        }
        String bpmId;
        if (idParamValue == null || idParamValue.length == 0) {
            throw new IllegalArgumentException("The parameter \"id\" is missing from the original URL");
        } else {
            bpmId = idParamValue[0];
            try {
                final SFormMapping formMapping = formMappingService.get(key);
                final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(formMapping.getProcessDefinitionId());
                final String locale = (String) context.get(URLAdapterConstants.LOCALE);
                final String contextPath = (String) context.get(URLAdapterConstants.CONTEXT_PATH);
                boolean assignTask = false;
                final String[] assignTaskValue = queryParameters.get(URLAdapterConstants.ASSIGN_TASK_QUERY_PARAM);
                if (assignTaskValue != null && assignTaskValue.length > 0 && "true".equals(assignTaskValue[0])) {
                    assignTask = true;
                }
                String user = null;
                final String[] userParamValue = queryParameters.get(URLAdapterConstants.USER_QUERY_PARAM);
                if (userParamValue != null && userParamValue.length > 0) {
                    user = userParamValue[0];
                }
                String mode = DEFAULT_FORM_MODE;
                final String[] modeParamValue = queryParameters.get(URLAdapterConstants.MODE_QUERY_PARAM);
                if (modeParamValue != null && modeParamValue.length > 0) {
                    mode = modeParamValue[0];
                }
                boolean autoInstantiate = true;
                final String[] autoInstantiateValue = queryParameters.get(URLAdapterConstants.AUTO_INSTANTIATE_QUERY_PARAM);
                if (autoInstantiateValue != null && autoInstantiateValue.length > 0 && "false".equals(autoInstantiateValue[0])) {
                    autoInstantiate = false;
                }
                return generateLegacyURL(contextPath, locale, bpmId, formMapping, processDefinition, user, assignTask, mode, autoInstantiate);
            } catch (final SBonitaException e) {
                throw new SExecutionException("Unable to generate the legacy form URL for key " + key + "(id: " + bpmId + ")", e);
            }
        }
    }

    protected String generateLegacyURL(final String contextPath, final String locale, final String bpmId, final SFormMapping formMapping, final SProcessDefinition processDefinition, final String user, final boolean assignTask, final String mode, boolean autoInstantiate) {
        final StringBuilder legacyFormURL = new StringBuilder(contextPath);
        legacyFormURL.append("/portal/homepage?ui=form&locale=")
            .append(locale)
            .append("&theme=")
            .append(formMapping.getProcessDefinitionId())
            .append("#mode=")
            .append(mode)
            .append("&form=")
            .append(urlEncode(processDefinition.getName()))
            .append(UUID_SEPERATOR)
            .append(urlEncode(processDefinition.getVersion()));
        if (FormMappingType.TASK.getId().equals(formMapping.getType())) {
            legacyFormURL.append(UUID_SEPERATOR).append(urlEncode(formMapping.getTask() + "$"))
                .append("entry&task=")
                .append(bpmId);
            if (assignTask) {
                legacyFormURL.append("&assignTask=true");
            }
        } else if (FormMappingType.PROCESS_OVERVIEW.getId().equals(formMapping.getType())) {
            legacyFormURL.append(urlEncode("$"))
                .append("recap&instance=").append(bpmId)
                .append("&recap=true");
        } else {
            legacyFormURL.append(urlEncode("$"))
                .append("entry&process=").append(bpmId);
            if (!autoInstantiate) {
                legacyFormURL.append("&autoInstantiate=false");
            }
        }
        if (user != null) {
            legacyFormURL.append("&userId=").append(user);
        }
        return legacyFormURL.toString();
    }

    protected String urlEncode(final String stringToEncode) {
        try {
            return URLEncoder.encode(stringToEncode, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public String getId() {
        return URLAdapterConstants.LEGACY_URL_ADAPTER;
    }
}
