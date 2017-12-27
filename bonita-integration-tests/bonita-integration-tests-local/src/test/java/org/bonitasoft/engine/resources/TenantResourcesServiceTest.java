/**
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.engine.resources;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class TenantResourcesServiceTest extends CommonBPMServicesTest {

    private static TenantResourcesService tenantResourcesService;

    private static TransactionService transactionService;

    @Before
    public void before() {
        tenantResourcesService = getTenantAccessor().getTenantResourcesService();
        transactionService = getTransactionService();
    }

    @After
    public void after() throws Exception {
        transactionService.executeInTransaction(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                tenantResourcesService.removeAll(TenantResourceType.BDM);
                return null;
            }
        });
    }

    @Test
    public void should_create_and_get_resource_work() throws Exception {
        transactionService.begin();
        //given
        tenantResourcesService.add("myResource", TenantResourceType.BDM, "theResourceContent".getBytes(), -1);
        //when
        transactionService.complete();
        transactionService.begin();
        STenantResource myResource = tenantResourcesService.get(TenantResourceType.BDM, "myResource");
        //then
        assertThat(myResource.getName()).isEqualTo("myResource");
        assertThat(myResource.getType()).isEqualTo(TenantResourceType.BDM);
        assertThat(new String(myResource.getContent())).isEqualTo("theResourceContent");
        transactionService.complete();
    }

}
