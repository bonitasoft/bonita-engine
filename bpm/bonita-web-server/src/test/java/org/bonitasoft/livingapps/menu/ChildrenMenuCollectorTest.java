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

public class ChildrenMenuCollectorTest {

    ChildrenMenuCollector collector = new ChildrenMenuCollector(1L);

    ApplicationMenuImpl menu = new ApplicationMenuImpl("name", 1L, 2L, 1);

    @Test
    public void should_be_collectible_when_menu_parentId_is_the_given_parentId() {
        menu.setParentId(1L);

        assertThat(collector.isCollectible(menu)).isTrue();
    }

    @Test
    public void should_not_be_collectible_when_menu_parentId_is_not_the_given_parentId() {
        menu.setParentId(2L);

        assertThat(collector.isCollectible(menu)).isFalse();
    }

    @Test
    public void should_not_be_collectible_when_menu_parentId_is_null() {
        menu.setParentId(null);

        assertThat(collector.isCollectible(menu)).isFalse();
    }
}
