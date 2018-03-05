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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.security.NoSuchAlgorithmException;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.theme.model.SThemeType;
import org.bonitasoft.engine.theme.model.impl.SThemeImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ThemeActionCalculatorTest {

    @Mock
    private TechnicalLoggerService loggerService;

    @InjectMocks
    private ThemeActionCalculator actionCalculator;

    @Before
    public void setUp() throws Exception {
        given(loggerService.isLoggable(any(), any())).willReturn(true);

    }

    @Test
    public void calculateAction_should_return_create_when_current_theme_is_null() throws Exception {
        //given
        byte[] newContent = { 1 };

        //when
        ThemeActionCalculator.ThemeAction action = actionCalculator.calculateAction(null, newContent);

        //then
        assertThat(action).isEqualTo(ThemeActionCalculator.ThemeAction.CREATE);
    }

    @Test
    public void calculateAction_should_return_none_when_new_content_is_null() throws Exception {

        //when
        ThemeActionCalculator.ThemeAction action = actionCalculator.calculateAction(null, null);

        //then
        assertThat(action).isEqualTo(ThemeActionCalculator.ThemeAction.NONE);
    }

    @Test
    public void calculateAction_should_return_none_when_new_content_is_empty() throws Exception {
        //given
        byte[] newContent = { };

        //when
        ThemeActionCalculator.ThemeAction action = actionCalculator.calculateAction(null, newContent);

        //then
        assertThat(action).isEqualTo(ThemeActionCalculator.ThemeAction.NONE);
    }

    @Test
    public void calculate_should_return_update_when_current_theme_is_not_null_and_content_has_changed() throws Exception {
        //given
        byte[] oldContent = { 1 };
        byte[] newContent = { 2 };
        SThemeImpl theme = new SThemeImpl(oldContent, true, SThemeType.PORTAL, System.currentTimeMillis());

        //when
        ThemeActionCalculator.ThemeAction action = actionCalculator.calculateAction(theme, newContent);

        //then
        assertThat(action).isEqualTo(ThemeActionCalculator.ThemeAction.UPDATE);
    }

    @Test
    public void calculate_should_return_none_when_is_not_a_default_theme() throws Exception {
        //given
        byte[] oldContent = { 1 };
        byte[] newContent = { 2 };
        SThemeImpl theme = new SThemeImpl(oldContent, false, SThemeType.PORTAL, System.currentTimeMillis());

        //when
        ThemeActionCalculator.ThemeAction action = actionCalculator.calculateAction(theme, newContent);

        //then
        assertThat(action).isEqualTo(ThemeActionCalculator.ThemeAction.NONE);
    }

    @Test
    public void calculate_should_return_none_when_current_theme_is_not_null_and_content_has_not_changed() throws Exception {
        //given
        byte[] oldContent = { 1 };
        byte[] newContent = { 1 };
        SThemeImpl theme = new SThemeImpl(oldContent, true, SThemeType.PORTAL, System.currentTimeMillis());

        //when
        ThemeActionCalculator.ThemeAction action = actionCalculator.calculateAction(theme, newContent);

        //then
        assertThat(action).isEqualTo(ThemeActionCalculator.ThemeAction.NONE);
    }

    @Test
    public void calculate_should_log_message_when_unable_to_calculate_md5() throws Exception {
        //given
        actionCalculator = spy(actionCalculator);
        byte[] oldContent = { 1 };
        byte[] newContent = { 1 };
        SThemeImpl theme = new SThemeImpl(oldContent, true, SThemeType.PORTAL, System.currentTimeMillis());
        NoSuchAlgorithmException exception = new NoSuchAlgorithmException("msg");
        doThrow(exception).when(actionCalculator).md5(any());
        //when
        ThemeActionCalculator.ThemeAction action = actionCalculator.calculateAction(theme, newContent);

        //then
        assertThat(action).isEqualTo(ThemeActionCalculator.ThemeAction.NONE);
        verify(loggerService).log(any(), eq(TechnicalLogSeverity.ERROR),
                eq("Unable to verify if default theme 'PORTAL' has changed. It will not be updated."), eq(exception));
    }

    @Test
    public void calculate_should_return_none_when_the_new_content_is_null() throws Exception {
        //given
        byte[] oldContent = { 1 };
        SThemeImpl theme = new SThemeImpl(oldContent, true, SThemeType.PORTAL, System.currentTimeMillis());

        //when
        ThemeActionCalculator.ThemeAction action = actionCalculator.calculateAction(theme, null);

        //then
        assertThat(action).isEqualTo(ThemeActionCalculator.ThemeAction.NONE);
    }

    @Test
    public void calculate_should_return_none_when_the_new_content_is_empty() throws Exception {
        //given
        byte[] oldContent = { 1 };
        byte[] newContent = {};
        SThemeImpl theme = new SThemeImpl(oldContent, true, SThemeType.PORTAL, System.currentTimeMillis());

        //when
        ThemeActionCalculator.ThemeAction action = actionCalculator.calculateAction(theme, newContent);

        //then
        assertThat(action).isEqualTo(ThemeActionCalculator.ThemeAction.NONE);
    }

}
