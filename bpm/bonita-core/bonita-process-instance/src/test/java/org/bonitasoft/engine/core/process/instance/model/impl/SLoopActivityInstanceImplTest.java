/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;
import org.junit.Before;
import org.junit.Test;

public class SLoopActivityInstanceImplTest {

    private SLoopActivityInstance loop;

    @Before
    public void setUp() {
        loop = new SLoopActivityInstance();
    }

    @Test
    public void mustExecuteOnAbortOrCancelProcess_return_false_when_is_stable() {
        // given
        loop.setStable(true);

        // when
        boolean executeOnAbortOrCancelProcess = loop.mustExecuteOnAbortOrCancelProcess();

        // then
        assertThat(executeOnAbortOrCancelProcess).isFalse();
    }

    @Test
    public void mustExecuteOnAbortOrCancelProcess_return_false_when_is_not_stable() {
        // given
        loop.setStable(false);

        // when
        boolean executeOnAbortOrCancelProcess = loop.mustExecuteOnAbortOrCancelProcess();

        // then
        assertThat(executeOnAbortOrCancelProcess).isFalse();
    }

}
