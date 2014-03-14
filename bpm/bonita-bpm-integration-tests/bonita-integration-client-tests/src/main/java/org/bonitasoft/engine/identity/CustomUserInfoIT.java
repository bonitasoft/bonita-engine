/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.exception.BonitaException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class CustomUserInfoIT extends CommonAPITest {
    
    @Before
    public void before() throws BonitaException {
        login();
    }

    @After
    public void after() throws BonitaException {
        logout();
    }
    
    @Test
    public void createCustomUserInfoDefinition_should_return_the_new_created_object() throws Exception {
        //given
        String infoName = "skills";
        String infoDescription = "the user skills";
        CustomUserInfoDefinitionCreator creator = new CustomUserInfoDefinitionCreator(infoName, infoDescription);

        //when
        CustomUserInfoDefinition info = getIdentityAPI().createCustomUserInfoDefinition(creator);

        //then
        assertThat(info.getName()).isEqualTo(infoName);
        assertThat(info.getDescription()).isEqualTo(infoDescription);
    }
    

}
