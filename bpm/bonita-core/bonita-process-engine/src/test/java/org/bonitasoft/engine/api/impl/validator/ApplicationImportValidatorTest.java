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

package org.bonitasoft.engine.api.impl.validator;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.exception.ImportException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationImportValidatorTest {

    @Mock
    private ApplicationTokenValidator tokenValidator;

    @InjectMocks
    private ApplicationImportValidator importValidator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void validate_should_do_nothing_when_token_is_valid() throws Exception {
        //given
        given(tokenValidator.validate("aToken")).willReturn(new ValidationStatus(true));

        //when
        importValidator.validate("aToken");

        //then
        verify(tokenValidator).validate("aToken");
    }

    @Test
    public void validate_should_throw_ImportException_when_token_is_invalid() throws Exception {
        //given
        given(tokenValidator.validate("aToken")).willReturn(new ValidationStatus(false, "Invalid"));

        //then
        expectedException.expect(ImportException.class);
        expectedException.expectMessage("Invalid");

        //when
        importValidator.validate("aToken");
    }

}
