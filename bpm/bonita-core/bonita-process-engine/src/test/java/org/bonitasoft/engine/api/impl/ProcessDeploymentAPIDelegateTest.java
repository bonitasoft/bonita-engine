/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.InputStream;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessDeploymentAPIDelegateTest {

    @Spy
    ProcessDeploymentAPIDelegate processDeploymentAPIDelegate;

    @Test
    public void validateBusinessArchive_should_throw_exception_if_empty_file_detected() throws Exception {
        try (final InputStream resourceAsStream = this.getClass().getResourceAsStream("EmptyDocument--1.0.bar")) {
            final BusinessArchive businessArchive = BusinessArchiveFactory.readBusinessArchive(resourceAsStream);

            assertThatThrownBy(() -> processDeploymentAPIDelegate.validateBusinessArchive(businessArchive))
                    .isInstanceOf(ProcessDeployException.class).hasMessage(
                            "The BAR file you are trying to deploy contains an empty file: resources/forms/resources/emptyDocument2.pdf. The process cannot be deployed. Fix it or remove it from the BAR.");
        }
    }

    @Test
    public void deploy_should_not_call_service_deploy_if_bar_validation_failed() throws Exception {
        // given:
        doThrow(ProcessDeployException.class).when(processDeploymentAPIDelegate)
                .validateBusinessArchive(any(BusinessArchive.class));
        final BusinessArchive businessArchive = mock(BusinessArchive.class);

        // when:
        assertThatThrownBy(() -> processDeploymentAPIDelegate.deploy(businessArchive))
                .isInstanceOf(ProcessDeployException.class);

        // then:
        verifyNoMoreInteractions(businessArchive);
        verify(processDeploymentAPIDelegate, times(0)).getServiceAccessor();
    }

}
