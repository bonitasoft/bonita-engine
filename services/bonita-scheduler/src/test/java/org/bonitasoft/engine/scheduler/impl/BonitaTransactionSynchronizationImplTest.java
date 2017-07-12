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
package org.bonitasoft.engine.scheduler.impl;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class BonitaTransactionSynchronizationImplTest {

    @Mock
    private SessionAccessor sessionAccessor;

    @InjectMocks
    private BonitaTransactionSynchronizationImpl bonitaTransactionSynchronizationImpl;

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.BonitaTransactionSynchronizationImpl#beforeCommit()}.
     */
    @Test
    public final void beforeCommit_do_nothing() {
        // When
        bonitaTransactionSynchronizationImpl.beforeCommit();

        // Then
        verify(sessionAccessor, never()).deleteTenantId();
        verify(sessionAccessor, never()).setTenantId(anyLong());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.scheduler.impl.BonitaTransactionSynchronizationImpl#afterCompletion(org.bonitasoft.engine.transaction.TransactionState)}.
     */
    @Test
    public final void afterCompletion_should_deleteTenantId_in_session() {
        // When
        bonitaTransactionSynchronizationImpl.afterCompletion(null);

        // Then
        verify(sessionAccessor).deleteTenantId();
    }

}
