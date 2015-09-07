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
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.session.APISession;

/**
 * @author mazourd
 */
public class EngineInitializer {

    private static EngineInitializer factory;

    public static final String DEFAULT_TECHNICAL_LOGGER_USERNAME = "install";

    public static final String DEFAULT_TECHNICAL_LOGGER_PASSWORD = "install";

    private ProcessAPI processAPI;
    private IdentityAPI identityAPI;
    private UserTaskAPI userTaskAPI;
    private static EngineStarter engineStarter = new EngineStarter();

    public EngineInitializer() {

    }

    public static EngineInitializer getInstance() {
        if (factory == null) {
            factory = new EngineInitializer();
        }
        return factory;
    }

    public static void startEngine() throws Exception {
        engineStarter.startEngine();
    }
    public static void stopEngine() throws Exception {
        engineStarter.stopEngine();
    }

    public void defaultLogin() throws Exception {
        getUserTaskAPI(TenantAPIAccessor.getLoginAPI().login(DEFAULT_TECHNICAL_LOGGER_USERNAME,
                DEFAULT_TECHNICAL_LOGGER_PASSWORD));
        createProcessAPIandIdentityAPIfromSession(TenantAPIAccessor.getLoginAPI().login(DEFAULT_TECHNICAL_LOGGER_USERNAME,
                DEFAULT_TECHNICAL_LOGGER_PASSWORD));
    }

    public APITestProcessCleaner getProcessCleaner(ProcessAPI processAPI){
        return new APITestProcessCleanerImpl(processAPI);
    }

    public APITestProcessCleaner getProcessCleaner(){
        return new APITestProcessCleanerImpl(processAPI);
    }

    public UserTaskAPI getUserTaskAPI(APISession session) throws Exception {
        UserTaskAPIImpl userTaskAPI = new UserTaskAPIImpl();
        userTaskAPI.usingSession(session);
        this.userTaskAPI = userTaskAPI;
        return userTaskAPI;
    }


    public APITestProcessAnalyser getAPITestProcessAnalyser(ProcessAPI processAPI) {
        return new APITestProcessAnalyserImpl(processAPI,userTaskAPI);
    }


    public APITestProcessAnalyser getAPITestProcessAnalyser() {
        return new APITestProcessAnalyserImpl(processAPI,userTaskAPI);
    }


    public IdentityAnalyserTestAPI getIdentityBuilderAPI(IdentityAPI identityAPI) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException, LoginException {
        return new IdentityAnalyserTestAPIImpl(identityAPI);
    }

    public IdentityAnalyserTestAPI getIdentityBuilderAPI(){
        return new IdentityAnalyserTestAPIImpl(identityAPI);
    }


    private void createProcessAPIandIdentityAPIfromSession(APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        this.processAPI = TenantAPIAccessor.getProcessAPI(session);
        this.identityAPI = TenantAPIAccessor.getIdentityAPI(session);
    }

    public ProcessDeployerAPITest getProcessDeployer(){
        return new ProcessDeployerAPITest(processAPI);
    }

    public EngineInitializer usingProcessAPI(ProcessAPI processAPI) {
        this.processAPI = processAPI;
        return this;
    }

    public UserTaskAPI getUserTaskAPI() {
        return userTaskAPI;
    }

    public void setUserTaskAPI(UserTaskAPI userTaskAPI) {
        this.userTaskAPI = userTaskAPI;
    }

    public ProcessAPI getProcessAPI() {
        return processAPI;
    }

    public IdentityAPI getIdentityAPI() {
        return identityAPI;
    }

}
