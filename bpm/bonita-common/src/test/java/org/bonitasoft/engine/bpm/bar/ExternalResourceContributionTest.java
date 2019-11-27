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
package org.bonitasoft.engine.bpm.bar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExternalResourceContributionTest {

    @Mock
    BusinessArchive businessArchive;

    @Test
    public void should_not_be_mandatory_contribution() {
        //given
        ExternalResourceContribution externalResourceContribution = new ExternalResourceContribution();

        //when then
        assertThat(externalResourceContribution.isMandatory()).isFalse();

    }

    @Test
    public void should_have_a_name() {
        //given
        ExternalResourceContribution externalResourceContribution = new ExternalResourceContribution();

        //when then
        assertThat(externalResourceContribution.getName()).isEqualTo("resources");
    }

    @Test
    public void should_retrieve_custom_pages_from_bar() throws Exception {
        //given
        ExternalResourceContribution externalResourceContribution = new ExternalResourceContribution();
        File barFolder = Paths.get(this.getClass().getResource("/barRoot").toURI()).toFile();

        //when
        externalResourceContribution.readFromBarFolder(businessArchive, barFolder);

        //then
        verify(businessArchive).addResource(eq("resources/customPages/custompage_step1.zip"), notNull());
        verify(businessArchive).addResource(eq("resources/customPages/custompage_step2.zip"), notNull());
    }
}
