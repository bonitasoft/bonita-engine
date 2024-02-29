/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.internal.jta.xa.TxInfo;
import org.junit.Test;

public class XAResourceRetrieverTest {

    @Test
    public void should_return_2_resources_when_arjuna_transaction_manager_has_2_resources() {
        com.arjuna.ats.jta.transaction.Transaction transaction = mock(com.arjuna.ats.jta.transaction.Transaction.class);
        Map<XAResource, TxInfo> xaResourceTxInfoMap = new HashMap<>();
        xaResourceTxInfoMap.put(mock(XAResource.class), mock(TxInfo.class));
        xaResourceTxInfoMap.put(mock(XAResource.class), mock(TxInfo.class));
        doReturn(xaResourceTxInfoMap).when(transaction).getResources();

        Optional<List<XAResource>> xaResources = new XAResourceRetriever().retrieveResources(transaction);

        assertThat(xaResources.get()).hasSize(2);
    }

    @Test
    public void should_return_1_resource_when_arjuna_transaction_manager_has_1_resource() {
        com.arjuna.ats.jta.transaction.Transaction transaction = mock(com.arjuna.ats.jta.transaction.Transaction.class);
        Map<XAResource, TxInfo> xaResourceTxInfoMap = new HashMap<>();
        xaResourceTxInfoMap.put(mock(XAResource.class), mock(TxInfo.class));
        doReturn(xaResourceTxInfoMap).when(transaction).getResources();

        Optional<List<XAResource>> xaResources = new XAResourceRetriever().retrieveResources(transaction);

        assertThat(xaResources.get()).hasSize(1);
    }

    @Test
    public void should_return_no_value_when_transaction_manager_is_not_arjuna() throws NoSuchMethodException {
        XAResourceRetriever xaResourceRetriever = new XAResourceRetriever();

        Transaction transaction = mock(Transaction.class);

        Optional<List<XAResource>> xaResources = xaResourceRetriever.retrieveResources(transaction);
        assertThat(xaResources).isEmpty();
    }

    @Test
    public void should_return_no_value_resource_when_transaction_object_is_not_arjuna() {
        com.arjuna.ats.jta.transaction.Transaction transaction = mock(com.arjuna.ats.jta.transaction.Transaction.class);
        doThrow(new ClassCastException()).when(transaction).getResources();

        Optional<List<XAResource>> xaResources = new XAResourceRetriever().retrieveResources(transaction);

        assertThat(xaResources).isEmpty();
    }

    @Test
    public void should_not_retry_getResource_when_first_call_failed() {
        com.arjuna.ats.jta.transaction.Transaction transaction = mock(com.arjuna.ats.jta.transaction.Transaction.class);
        doThrow(new ClassCastException()).when(transaction).getResources();

        XAResourceRetriever xaResourceRetriever = new XAResourceRetriever();

        assertThat(xaResourceRetriever.retrieveResources(transaction)).isEmpty();
        assertThat(xaResourceRetriever.retrieveResources(transaction)).isEmpty();

        verify(transaction, times(1)).getResources();
    }

}
