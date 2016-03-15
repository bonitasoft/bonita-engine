/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.xml.parse;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.bonitasoft.engine.xml.ElementBinding;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class BindingHandlerTest {

    private BindingHandler bindingHandler = new BindingHandler(Collections.<Class<? extends ElementBinding>>emptyList());

    @Test
    public void should_character_do_not_skip_line_return() throws Exception {
        //given
        char[] content = "start  \n  end.".toCharArray();
        //when
        bindingHandler.characters(content, 0, 5);
        bindingHandler.characters(content, 5, 5);
        bindingHandler.characters(content, 10, 4);

        //then
        assertThat(bindingHandler.tempVal.toString()).isEqualTo("start  \n  end.");
    }
}