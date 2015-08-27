/*******************************************************************************
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
 ******************************************************************************/

package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessExecutionException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.session.APISession;

/**
 * @author mazourd
 */
public class EngineInitializerAPIFactoryImpl implements EngineInitializerAPIFactory {

    private static EngineInitializerAPIFactoryImpl factory;

    public static final String DEFAULT_TECHNICAL_LOGGER_USERNAME = "install";

    public static final String DEFAULT_TECHNICAL_LOGGER_PASSWORD = "install";

    private ProcessAPI processAPI;
    private IdentityAPI identityAPI;
    private UserTaskAPI userTaskAPI;

    public EngineInitializerAPIFactoryImpl() {

    }

    public static EngineInitializerAPIFactoryImpl getInstance() {
        if (factory == null) {
            factory = new EngineInitializerAPIFactoryImpl();
        }
        return factory;
    }
    public StartedProcess startProcess(long processDefintionId) throws ProcessDefinitionNotFoundException, ProcessExecutionException, ProcessActivationException {
        ProcessInstance processInstance = processAPI.startProcess(processDefintionId);
        StartedProcess startedProcess = new StartedProcess(processAPI,processInstance, userTaskAPI);
        return startedProcess;
    }

    @Override
    public APITestProcessCleaner getProcessCleaner(ProcessAPI processAPI){
        return new APITestProcessCleanerImpl(processAPI);
    }
    @Override
    public APITestProcessCleaner getProcessCleaner(){
        return new APITestProcessCleanerImpl(processAPI);
    }
    @Override
    public UserTaskAPI getUserTaskAPI(APISession session) throws Exception {
        UserTaskAPIImpl userTaskAPI = new UserTaskAPIImpl();
        userTaskAPI.usingSession(session);
        this.userTaskAPI = userTaskAPI;
        return userTaskAPI;
    }

    @Override
    public APITestProcessAnalyser getAPITestProcessAnalyser(ProcessAPI processAPI) {
        return new APITestProcessAnalyserImpl(processAPI);
    }

    @Override
    public APITestProcessAnalyser getAPITestProcessAnalyser() {
        return new APITestProcessAnalyserImpl(processAPI);
    }

    @Override
    public IdentityAnalyserTestAPI getIdentityBuilderAPI(IdentityAPI identityAPI) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException, LoginException {
        return new IdentityAnalyserTestAPIImpl(identityAPI);
    }
    @Override
    public IdentityAnalyserTestAPI getIdentityBuilderAPI(){
        return new IdentityAnalyserTestAPIImpl(identityAPI);
    }

    @Override
    public void createProcessAPIandIdentityAPIfromSession(APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        this.processAPI = TenantAPIAccessor.getProcessAPI(session);
        this.identityAPI = TenantAPIAccessor.getIdentityAPI(session);
    }

    @Override
    public EngineInitializerAPIFactoryImpl usingProcessAPI(ProcessAPI processAPI) {
        this.processAPI = processAPI;
        return this;
    }

    public UserTaskAPI getUserTaskAPI() {
        return userTaskAPI;
    }

    public void setUserTaskAPI(UserTaskAPI userTaskAPI) {
        this.userTaskAPI = userTaskAPI;
    }
}
