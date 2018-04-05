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

package org.bonitasoft.engine.platform.model.builder.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.junit.Test;

public class SPlatformBuilderFactoryImplTest {

    @Test
    public void getInformationKey_should_return_string_information() throws Exception {
        //given
        SPlatformBuilderFactoryImpl builderFactory = new SPlatformBuilderFactoryImpl();

        //then
        assertThat(builderFactory.getInformationKey()).isEqualTo("information");
    }


    @Test
    public void testPlatformBuilder() {
        final String version = "myVersion";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();
        final String initialVersion = "initialVersion";
        final String previousVersion = "previousVersion";

        final SPlatformBuilder sPlatformBuilder = new SPlatformBuilderFactoryImpl().createNewInstance(version, previousVersion, initialVersion,
                createdBy, created);

        final SPlatform platform = sPlatformBuilder.done();

        assertEquals(version, platform.getVersion());
        assertEquals(createdBy, platform.getCreatedBy());
        assertEquals(created, platform.getCreated());
        assertEquals(initialVersion, platform.getInitialVersion());
        assertEquals(previousVersion, platform.getPreviousVersion());
    }

}