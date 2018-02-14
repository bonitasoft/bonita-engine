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

package org.bonitasoft.engine.core.process.instance.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SGatewayInstanceImplTest {

    @Test
    public void isFinished_should_return_true_when_hitbys_starts_with_FINISH() throws Exception {
        //given
        SGatewayInstanceImpl gatewayInstance = buildGateWithHitBys("FINISH:1");

        //when
        gatewayInstance.isFinished();

        //then
        assertThat(gatewayInstance.isFinished()).isTrue();
    }

    @Test
    public void isFinished_should_return_false_when_hitbys_doesnt_start_with_FINISH() throws Exception {
        //given
        SGatewayInstanceImpl gatewayInstance = buildGateWithHitBys("1,2");

        //when
        gatewayInstance.isFinished();

        //then
        assertThat(gatewayInstance.isFinished()).isFalse();
    }

    @Test
    public void isFinished_should_return_false_when_hitbys_is_null() throws Exception {
        //given
        SGatewayInstanceImpl gatewayInstance = buildGateWithHitBys(null);

        //when
        gatewayInstance.isFinished();

        //then
        assertThat(gatewayInstance.isFinished()).isFalse();
    }


    private SGatewayInstanceImpl buildGateWithHitBys(final String hitBys) {
        SGatewayInstanceImpl gatewayInstance = new SGatewayInstanceImpl();
        gatewayInstance.setHitBys(hitBys);
        return gatewayInstance;
    }

}