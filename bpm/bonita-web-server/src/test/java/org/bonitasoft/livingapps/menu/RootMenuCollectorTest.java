/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.livingapps.menu;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.business.application.impl.ApplicationMenuImpl;
import org.junit.Test;

public class RootMenuCollectorTest {

    RootMenuCollector collector = new RootMenuCollector();

    ApplicationMenuImpl menu = new ApplicationMenuImpl("name", 1L, 2L, 1);

    @Test
    public void should_be_collectible_when_the_menu_has_no_parentId() {
        menu.setParentId(null);

        assertThat(collector.isCollectible(menu)).isTrue();
    }

    @Test
    public void should_not_be_collectible_when_the_menu_has_a_parentId() {
        menu.setParentId(3L);

        assertThat(collector.isCollectible(menu)).isFalse();
    }
}
