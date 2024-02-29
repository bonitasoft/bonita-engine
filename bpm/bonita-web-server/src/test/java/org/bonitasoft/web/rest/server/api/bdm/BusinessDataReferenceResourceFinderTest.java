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
package org.bonitasoft.web.rest.server.api.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Arrays;

import org.bonitasoft.engine.business.data.impl.MultipleBusinessDataReferenceImpl;
import org.bonitasoft.engine.business.data.impl.SimpleBusinessDataReferenceImpl;
import org.junit.Test;

public class BusinessDataReferenceResourceFinderTest {

    private BusinessDataReferenceResourceFinder businessDataReferenceResourceFinder = new BusinessDataReferenceResourceFinder();

    @Test
    public void should_return_a_context_of_type_MultipleBusinessDataRef_for_a_given_task_instance() throws Exception {
        //given
        MultipleBusinessDataReferenceImpl bizDataRef = new MultipleBusinessDataReferenceImpl("Ticket",
                "com.acme.object.Ticket", Arrays.asList(7L, 8L));

        //when
        Serializable contextResultElement = businessDataReferenceResourceFinder.toClientObject(bizDataRef);

        //then
        assertThat(contextResultElement).isInstanceOf(MultipleBusinessDataReferenceClient.class);
        final MultipleBusinessDataReferenceClient businessDataReferenceClient = (MultipleBusinessDataReferenceClient) contextResultElement;
        assertThat(businessDataReferenceClient.getName()).isEqualTo("Ticket");
        assertThat(businessDataReferenceClient.getType()).isEqualTo("com.acme.object.Ticket");
        assertThat(businessDataReferenceClient.getStorageIds()).containsExactly(7L, 8L);
        assertThat(businessDataReferenceClient.getStorageIdsAsString()).containsExactly("7", "8");
        assertThat(businessDataReferenceClient.getLink())
                .isEqualTo("API/bdm/businessData/com.acme.object.Ticket/findByIds?ids=7,8");
    }

    @Test
    public void should_return_a_context_of_type_Simple_for_a_given_task_instance() throws Exception {
        //given
        SimpleBusinessDataReferenceImpl bizDataRef = new SimpleBusinessDataReferenceImpl("Ticket",
                "com.acme.object.Ticket", 8L);

        //when
        Serializable contextResultElement = businessDataReferenceResourceFinder.toClientObject(bizDataRef);

        //then
        assertThat(contextResultElement).isInstanceOf(SimpleBusinessDataReferenceClient.class);
        final SimpleBusinessDataReferenceClient businessDataReferenceClient = (SimpleBusinessDataReferenceClient) contextResultElement;
        assertThat(businessDataReferenceClient.getName()).isEqualTo("Ticket");
        assertThat(businessDataReferenceClient.getType()).isEqualTo("com.acme.object.Ticket");
        assertThat(businessDataReferenceClient.getStorageId()).isEqualTo(8L);
        assertThat(businessDataReferenceClient.getStorageIdAsString()).isEqualTo("8");
        assertThat(businessDataReferenceClient.getLink()).isEqualTo("API/bdm/businessData/com.acme.object.Ticket/8");
    }

    @Test
    public void should_handle_multiple_business_data() throws Exception {
        //given
        MultipleBusinessDataReferenceImpl bizDataRef = new MultipleBusinessDataReferenceImpl("Ticket",
                "com.acme.object.Ticket", Arrays.asList(7L, 8L));

        //when
        boolean handlesResource = businessDataReferenceResourceFinder.handlesResource(bizDataRef);

        //then
        assertThat(handlesResource).isTrue();
    }

    @Test
    public void should_handle_simple_business_data() throws Exception {
        //given
        SimpleBusinessDataReferenceImpl bizDataRef = new SimpleBusinessDataReferenceImpl("Ticket",
                "com.acme.object.Ticket", 8L);

        //when
        boolean handlesResource = businessDataReferenceResourceFinder.handlesResource(bizDataRef);

        //then
        assertThat(handlesResource).isTrue();
    }

    @Test
    public void should_not_handle_other_types() throws Exception {
        //when
        boolean handlesResource = businessDataReferenceResourceFinder.handlesResource(12l);

        //then
        assertThat(handlesResource).isFalse();
    }

}
