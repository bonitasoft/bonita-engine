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

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.util.APITypeManager;
import org.junit.Test;

public class TenantAccessorTest {

    @Cover(classes = { APITypeManager.class, ApiAccessType.class }, concept = BPMNConcept.NONE, keywords = { "API" }, exceptions = { ServerAPIException.class }, story = "Set the API Type and parameters and check it's correctly set.", jira = "ENGINE-451")
    @Test
    public void testSetAPITypeAndParams() throws Exception {
        final ApiAccessType apiType = APITypeManager.getAPIType();
        final Map<String, String> parameters = APITypeManager.getAPITypeParameters();
        TenantAPIAccessor.refresh();

        TenantAPIAccessor.getLoginAPI();
        final Map<String, String> passedParameters = new HashMap<String, String>();
        passedParameters.put("NawakKey", "NawakValue");
        APITypeManager.setAPITypeAndParams(ApiAccessType.EJB3, passedParameters);
        try {
            TenantAPIAccessor.getLoginAPI();
        } catch (final ServerAPIException e) {
            e.printStackTrace();
            return;
        } finally {
            APITypeManager.setAPITypeAndParams(apiType, parameters);
            TenantAPIAccessor.getLoginAPI();
        }

        fail("This statement should not be reached.");
    }

}
