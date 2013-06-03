package org.bonitasoft.engine.accessors;

import static org.junit.Assert.fail;

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
            APITypeManager.setAPITypeAndParams(apiType, parameters);
            TenantAPIAccessor.getLoginAPI();
            return;
        }

        fail("This statement should not be reached.");
    }

}
