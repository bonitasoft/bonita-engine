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
package org.bonitasoft.web.rest.server;

import java.util.List;
import java.util.logging.Level;

import org.bonitasoft.web.rest.server.api.bdm.BusinessDataFindByIdsResource;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataModelResource;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataQueryResource;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataReferenceResource;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataReferencesResource;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataResource;
import org.bonitasoft.web.rest.server.api.bpm.cases.ArchivedCaseContextResource;
import org.bonitasoft.web.rest.server.api.bpm.cases.ArchivedCaseVariableResource;
import org.bonitasoft.web.rest.server.api.bpm.cases.ArchivedCaseVariablesResource;
import org.bonitasoft.web.rest.server.api.bpm.cases.CaseContextResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.ActivityVariableResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.TimerEventTriggerResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.UserTaskContextResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.UserTaskContractResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.UserTaskExecutionResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.archive.ArchivedActivityVariableResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.archive.ArchivedUserTaskContextResource;
import org.bonitasoft.web.rest.server.api.bpm.message.BPMMessageResource;
import org.bonitasoft.web.rest.server.api.bpm.process.ProcessContractResource;
import org.bonitasoft.web.rest.server.api.bpm.process.ProcessDefinitionDesignResource;
import org.bonitasoft.web.rest.server.api.bpm.process.ProcessInstantiationResource;
import org.bonitasoft.web.rest.server.api.bpm.signal.BPMSignalResource;
import org.bonitasoft.web.rest.server.api.form.FormMappingResource;
import org.bonitasoft.web.rest.server.api.system.I18nTranslationResource;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.engine.Engine;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

/**
 * @author Matthieu Chaffotte
 */
public class BonitaRestletApplication extends Application {

    public static final String ROUTER_EXTENSION_PREFIX = "/extension/";

    public static final String BDM_DEFINITION_URL = "/tenant/bdm";

    public static final String BDM_BUSINESS_DATA_URL = "/bdm/businessData";

    public static final String BDM_BUSINESS_DATA_REFERENCE_URL = "/bdm/businessDataReference";

    public static final String FORM_MAPPING_URL = "/form/mapping";

    public static final String BPM_PROCESS_URL = "/bpm/process";

    public static final String BPM_USER_TASK_URL = "/bpm/userTask";

    public static final String BPM_ARCHIVED_USER_TASK_URL = "/bpm/archivedUserTask";

    public static final String BPM_TIMER_EVENT_TRIGGER_URL = "/bpm/timerEventTrigger";

    public static final String BPM_MESSAGE_URL = "/bpm/message";

    public static final String BPM_SIGNAL_URL = "/bpm/signal";

    public static final String BPM_ACTIVITY_VARIABLE_URL = "/bpm/activityVariable";

    public static final String BPM_CASE_CONTEXT_URL = "/bpm/case";

    private static final String BPM_ARCHIVED_CASE_CONTEXT_URL = "/bpm/archivedCase";

    public static final String BPM_ARCHIVED_CASE_VARIABLE_URL = "/bpm/archivedCaseVariable";

    public static final String BPM_ARCHIVED_ACTIVITY_VARIABLE_URL = "/bpm/archivedActivityVariable";

    private final FinderFactory factory;

    public BonitaRestletApplication(final FinderFactory finderFactory, ConverterHelper converterHelper) {
        super();
        factory = finderFactory;
        getMetadataService().setDefaultMediaType(MediaType.APPLICATION_JSON);
        getMetadataService().setDefaultCharacterSet(CharacterSet.UTF_8);
        replaceJacksonConverter(converterHelper);
    }

    private void replaceJacksonConverter(ConverterHelper converterHelper) {
        final List<ConverterHelper> registeredConverters = Engine.getInstance().getRegisteredConverters();
        registeredConverters.add(converterHelper);
        for (ConverterHelper registeredConverter : registeredConverters) {
            if (registeredConverter.getClass().equals(JacksonConverter.class)) {
                registeredConverters.remove(registeredConverter);
                registeredConverters.add(converterHelper);
            }
        }
    }

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createInboundRoot() {
        return buildRouter();
    }

    protected Router buildRouter() {
        final Context context = getContext();
        final Router router = new Router(context);
        // WARNING: if you add a route you need to declare it in org.bonitasoft.web.rest.server.FinderFactory

        // GET an activityData:
        router.attach(BPM_ACTIVITY_VARIABLE_URL + "/{" + ActivityVariableResource.ACTIVITYDATA_ACTIVITY_ID + "}/{"
                + ActivityVariableResource.ACTIVITYDATA_DATA_NAME
                + "}", factory.create(ActivityVariableResource.class));

        // GET to search timer event triggers:
        router.attach(BPM_TIMER_EVENT_TRIGGER_URL, factory.create(TimerEventTriggerResource.class));
        // PUT to update timer event trigger date:
        router.attach(BPM_TIMER_EVENT_TRIGGER_URL + "/{" + TimerEventTriggerResource.ID_PARAM_NAME + "}",
                factory.create(TimerEventTriggerResource.class));

        // POST to send a BPM message to the engine:
        router.attach(BPM_MESSAGE_URL, factory.create(BPMMessageResource.class));

        // POST to send a BPM signal to the engine:
        router.attach(BPM_SIGNAL_URL, factory.create(BPMSignalResource.class));

        // GET to retrieve a case context:
        router.attach(BPM_CASE_CONTEXT_URL + "/{caseId}/context", factory.create(CaseContextResource.class));

        // GET to retrieve an archived case context
        router.attach(BPM_ARCHIVED_CASE_CONTEXT_URL + "/{archivedCaseId}/context",
                factory.create(ArchivedCaseContextResource.class));

        // GET a task contract:
        router.attach(BPM_USER_TASK_URL + "/{taskId}/contract", factory.create(UserTaskContractResource.class));
        // POST to execute a task with contract:
        router.attach(BPM_USER_TASK_URL + "/{taskId}/execution", factory.create(UserTaskExecutionResource.class));
        // GET to retrieve a task context:
        router.attach(BPM_USER_TASK_URL + "/{taskId}/context", factory.create(UserTaskContextResource.class));

        // GET an archived task context:
        router.attach(BPM_ARCHIVED_USER_TASK_URL + "/{archivedTaskId}/context",
                factory.create(ArchivedUserTaskContextResource.class));

        // GET a process defintion design :
        router.attach(BPM_PROCESS_URL + "/{processDefinitionId}/design",
                factory.create(ProcessDefinitionDesignResource.class));
        // GET a process contract:
        router.attach(BPM_PROCESS_URL + "/{processDefinitionId}/contract",
                factory.create(ProcessContractResource.class));
        // POST to instantiate a process with contract:
        router.attach(BPM_PROCESS_URL + "/{processDefinitionId}/instantiation",
                factory.create(ProcessInstantiationResource.class));

        // GET to search form mappings:
        router.attach(FORM_MAPPING_URL, factory.create(FormMappingResource.class));

        // GET the BDM status
        router.attach(BDM_DEFINITION_URL, factory.create(BusinessDataModelResource.class));

        //GET a BusinessData
        router.attach(BDM_BUSINESS_DATA_URL + "/{className}/findByIds",
                factory.create(BusinessDataFindByIdsResource.class));
        router.attach(BDM_BUSINESS_DATA_URL + "/{className}", factory.create(BusinessDataQueryResource.class));
        router.attach(BDM_BUSINESS_DATA_URL + "/{className}/{id}", factory.create(BusinessDataResource.class));
        router.attach(BDM_BUSINESS_DATA_URL + "/{className}/{id}/{fieldName}",
                factory.create(BusinessDataResource.class));

        // GET a Multiple BusinessDataReference
        router.attach(BDM_BUSINESS_DATA_REFERENCE_URL, factory.create(BusinessDataReferencesResource.class));
        // GET a Simple BusinessDataReference
        router.attach(BDM_BUSINESS_DATA_REFERENCE_URL + "/{caseId}/{dataName}",
                factory.create(BusinessDataReferenceResource.class));

        // api extension
        router.attach(ROUTER_EXTENSION_PREFIX, factory.createExtensionResource(), Template.MODE_STARTS_WITH);

        // GET all translations
        router.attach("/system/i18ntranslation", factory.create(I18nTranslationResource.class));

        router.attach(BPM_ARCHIVED_CASE_VARIABLE_URL + "/{caseId}/{variableName}",
                factory.create(ArchivedCaseVariableResource.class));
        router.attach(BPM_ARCHIVED_CASE_VARIABLE_URL, factory.create(ArchivedCaseVariablesResource.class));

        router.attach(BPM_ARCHIVED_ACTIVITY_VARIABLE_URL + "/{activityId}/{variableName}",
                factory.create(ArchivedActivityVariableResource.class));

        return router;
    }

    @Override
    public void handle(final Request request, final Response response) {
        request.setLoggable(false);
        Engine.setLogLevel(Level.OFF);
        Engine.setRestletLogLevel(Level.OFF);
        super.handle(request, response);
    }
}
