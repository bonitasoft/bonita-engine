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
package org.bonitasoft.engine.core.form;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.page.URLAdapterConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class ExternalURLAdapterTest {

    ExternalURLAdapter urlAdapter = new ExternalURLAdapter();

    @Test
    public void adaptShouldAppendParameterNameValuePairs() throws Exception {
        Map<String, String[]> queryParametersMap = new HashMap<>();
        queryParametersMap.put("someKey", new String[] { "true" });
        queryParametersMap.put("someParam", new String[] { "17", "641" });

        Map<String, Serializable> context = new HashMap<>();
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParametersMap);

        String url = "http://internal.subdomain.company.net/myFormProviderApp/local";

        String newUrl = urlAdapter.adapt(url, null, context);

        assertThat(newUrl).isEqualTo(url + "?someKey=true&someParam=17,641");
    }

    @Test
    public void adaptShouldAppendParametersBeforeHashtagIfPresent() throws Exception {
        Map<String, String[]> queryParametersMap = new HashMap<>();
        queryParametersMap.put("someKey", new String[] { "true" });

        Map<String, Serializable> context = new HashMap<>();
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParametersMap);

        String newUrl = urlAdapter.adapt("http://internal.subdomain.company.net/myFormProviderApp/local#myAnchor", null, context);

        assertThat(newUrl).isEqualTo("http://internal.subdomain.company.net/myFormProviderApp/local?someKey=true#myAnchor");
    }

    @Test
    public void adaptShouldKeepExistingParametersIfPresent() throws Exception {
        Map<String, String[]> queryParametersMap = new HashMap<>();
        queryParametersMap.put("boniparam", new String[] { "bonitabpm" });

        Map<String, Serializable> context = new HashMap<>();
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParametersMap);

        String newUrl = urlAdapter.adapt("http://internal.subdomain.company.net?redirect=false#title17", null, context);

        assertThat(newUrl).isEqualTo("http://internal.subdomain.company.net?redirect=false&boniparam=bonitabpm#title17");
    }

    @Test
    public void adaptShouldSupportEmptyHashtag() throws Exception {
        Map<String, String[]> queryParametersMap = new HashMap<>();
        queryParametersMap.put("p", new String[] { "value" });

        Map<String, Serializable> context = new HashMap<>();
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParametersMap);

        String newUrl = urlAdapter.adapt("http://internal.subdomain.company.net/page#", null, context);

        assertThat(newUrl).isEqualTo("http://internal.subdomain.company.net/page?p=value#");
    }
}
