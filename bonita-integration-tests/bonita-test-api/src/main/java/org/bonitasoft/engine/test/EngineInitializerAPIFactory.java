/*
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
 */

package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.session.APISession;

/**
 * @author mazourd
 */
public interface EngineInitializerAPIFactory {

    APITestProcessCleaner getProcessCleaner(ProcessAPI processAPI);

    APITestProcessCleaner getProcessCleaner();

    IdentityAnalyserTestAPI getIdentityBuilderAPI();

    void createProcessAPIandIdentityAPIfromSession(APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException;

    UserTaskAPI getUserTaskAPI(APISession session) throws Exception;

    APITestProcessAnalyser getAPITestProcessAnalyser(ProcessAPI processAPI);

    APITestProcessAnalyser getAPITestProcessAnalyser();

    IdentityAnalyserTestAPI getIdentityBuilderAPI(IdentityAPI identityAPI) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException, LoginException;

    EngineInitializerAPIFactoryImpl usingProcessAPI(ProcessAPI processAPI);

}
