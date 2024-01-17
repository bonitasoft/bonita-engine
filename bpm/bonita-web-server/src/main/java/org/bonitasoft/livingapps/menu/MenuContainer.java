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

import java.util.List;

import org.bonitasoft.engine.business.application.ApplicationMenu;

public class MenuContainer implements Menu {

    private final ApplicationMenu menu;

    private final List<Menu> children;

    public MenuContainer(final ApplicationMenu menu, final List<Menu> children) {
        this.menu = menu;
        this.children = children;
    }

    @Override
    public String getHtml() {
        final StringBuilder builder = new StringBuilder()
                .append("<li class=\"dropdown\">")
                .append("<a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\">")
                .append(menu.getDisplayName()).append(" <span class=\"caret\"></span></a>")
                .append("<ul class=\"dropdown-menu\" role=\"menu\">");
        for (final Menu child : children) {
            builder.append(child.getHtml());
        }
        return builder.append("</ul></li>").toString();
    }
}
