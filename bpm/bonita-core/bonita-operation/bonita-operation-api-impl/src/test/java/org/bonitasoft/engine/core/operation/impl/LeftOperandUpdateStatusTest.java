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

package org.bonitasoft.engine.core.operation.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.junit.Test;

public class LeftOperandUpdateStatusTest {

    @Test
    public void shouldUpdate_on_assignment() throws Exception {
        //given
        LeftOperandUpdateStatus updateStatus = new LeftOperandUpdateStatus(SOperatorType.ASSIGNMENT);

        //then
        assertThat(updateStatus.shouldUpdate()).isTrue();
        assertThat(updateStatus.shouldDelete()).isFalse();
    }

    @Test
    public void shouldUpdate_on_java_method() throws Exception {
        //given
        LeftOperandUpdateStatus updateStatus = new LeftOperandUpdateStatus(SOperatorType.JAVA_METHOD);

        //then
        assertThat(updateStatus.shouldUpdate()).isTrue();
        assertThat(updateStatus.shouldDelete()).isFalse();
    }

    @Test
    public void shouldUpdate_on_XPath() throws Exception {
        //given
        LeftOperandUpdateStatus updateStatus = new LeftOperandUpdateStatus(SOperatorType.XPATH_UPDATE_QUERY);

        //then
        assertThat(updateStatus.shouldUpdate()).isTrue();
        assertThat(updateStatus.shouldDelete()).isFalse();
    }

    @Test
    public void shouldDelete_on_deletion() throws Exception {
        //given
        LeftOperandUpdateStatus updateStatus = new LeftOperandUpdateStatus(SOperatorType.DELETION);

        //then
        assertThat(updateStatus.shouldUpdate()).isFalse();
        assertThat(updateStatus.shouldDelete()).isTrue();
    }

}
