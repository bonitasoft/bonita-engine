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
package org.bonitasoft.engine.io.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import javax.xml.validation.Schema;

import org.junit.Before;
import org.junit.Test;

public class SchemaLoaderTest {

    private SchemaLoader loader;

    @Before
    public void setUp() {
        loader = new SchemaLoader();
    }

    @Test
    public void can_load_schema_with_spaces_in_the_path() throws Exception {
        //given
        final URL resource = SchemaLoader.class.getResource("/folder with space/ProcessDefinition.xsd");
        //when
        final Schema schema = loader.loadSchema(resource);

        //then
        assertThat(schema).isNotNull();
    }

}
