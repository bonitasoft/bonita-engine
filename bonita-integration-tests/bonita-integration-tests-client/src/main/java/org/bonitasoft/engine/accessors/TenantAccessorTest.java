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
package org.bonitasoft.engine.accessors;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.test.junit.BonitaEngineRule;
import org.bonitasoft.engine.util.APITypeManager;
import org.junit.Rule;
import org.junit.Test;

public class TenantAccessorTest {

    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create();

    @Test
    public void testSetAPITypeAndParams() throws Exception {
        final ApiAccessType apiType = APITypeManager.getAPIType();
        final Map<String, String> parameters = APITypeManager.getAPITypeParameters();
        TenantAPIAccessor.getLoginAPI();
        final Map<String, String> passedParameters = new HashMap<>();
        passedParameters.put("NawakKey", "NawakValue");
        APITypeManager.setAPITypeAndParams(ApiAccessType.EJB3, passedParameters);
        try {
            TenantAPIAccessor.getLoginAPI();
            fail("This statement should not be reached.");
        } catch (final ServerAPIException ignored) {
        } finally {
            APITypeManager.setAPITypeAndParams(apiType, parameters);
        }

    }

}
