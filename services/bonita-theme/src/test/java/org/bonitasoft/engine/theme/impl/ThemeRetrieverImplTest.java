/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.theme.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;
import org.bonitasoft.engine.theme.persistence.SelectDescriptorBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ThemeRetrieverImplTest {

    @Mock
    private ReadPersistenceService persistenceService;

    @InjectMocks
    private ThemeRetrieverImpl themeRetriever;

    @Test
    public void getTheme_should_return_the_result_of_persistenceService_selectOne() throws Exception {
        //given
        STheme theme = mock(STheme.class);
        SThemeType portal = SThemeType.PORTAL;
        boolean isDefault = true;
        given(persistenceService.selectOne(SelectDescriptorBuilder.getTheme(portal, isDefault))).willReturn(theme);

        //when
        STheme retrievedTheme = themeRetriever.getTheme(portal, isDefault);

        //then
        assertThat(retrievedTheme).isEqualTo(theme);
    }

}
