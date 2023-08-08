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
package org.bonitasoft.engine.test.junit;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.BonitaDatabaseConfiguration;
import org.bonitasoft.engine.test.TestEngine;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class BonitaEngineRuleTest {

    private final MyTestEngine testEngineToInject = new MyTestEngine();
    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.createWith(testEngineToInject);

    @Test
    public void should_TestEngine_be_started() {
        assertThat(testEngineToInject.isStarted).isTrue();
    }

    private static class MyTestEngine implements TestEngine {

        public boolean isStarted;

        @Override
        public boolean start() {
            isStarted = true;
            return false;
        }

        @Override
        public void stop() {
        }

        @Override
        public void clearData() {
        }

        @Override
        public void setDropOnStart(boolean dropOnStart) {
        }

        @Override
        public void setBonitaDatabaseProperties(BonitaDatabaseConfiguration database) {
        }

        @Override
        public void setBusinessDataDatabaseProperties(BonitaDatabaseConfiguration database) {
        }
    }
}
