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

package org.bonitasoft.engine.theme;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.bonitasoft.engine.test.persistence.repository.ThemeRepository;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ThemeTest {

    @Inject
    private ThemeRepository repository;

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Test
    public void should_get_theme_by_type() {
        final byte[] themeContent = "binaryContent".getBytes();
        repository.add(STheme.builder()
                .tenantId(77L)
                .id(397512L)
                .content(themeContent)
                .isDefault(true)
                .type(SThemeType.MOBILE).build());

        // This check is to ensure we added the annotation @Enumerated(EnumType.STRING) on theme type,
        // because by default Hibernate converts ENUMs with their ordinal value when storing to Database:
        repository.flush();
        final String theType = jdbcTemplate.queryForObject("select type from theme", String.class);
        assertThat(theType).isEqualTo("MOBILE");

        final STheme theme = repository.getThemeByType(77L, SThemeType.MOBILE);
        assertThat(theme.getId()).isEqualTo(397512L);
        assertThat(theme.getContent()).isEqualTo(themeContent);
    }

}
