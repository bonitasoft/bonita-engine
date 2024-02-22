/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DatabaseUrlParserTest {

    @Test
    public void should_parse_postgres_url() {
        String url = "jdbc:postgresql://localhost:32782/bonita";

        DatabaseUrlParser.DatabaseMetadata metadata = DatabaseUrlParser.parsePostgresUrl(url);

        assertThat(metadata.getDatabaseName()).isEqualTo("bonita");
        assertThat(metadata.getPort()).isEqualTo("32782");
        assertThat(metadata.getServerName()).isEqualTo("localhost");
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_there_is_no_match() {
        String url = "jdbc:otherDb://localhost:32782/bonita";

        DatabaseUrlParser.parsePostgresUrl(url);
    }

}
