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
package com.bonitasoft.engine.business.application.impl;

import org.bonitasoft.engine.bpm.internal.NamedElementImpl;

import com.bonitasoft.engine.business.application.ApplicationRoute;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationPageImpl extends NamedElementImpl implements ApplicationRoute {

    private static final long serialVersionUID = -8043272410231723583L;
    private final long applicationId;
    private final long pageId;
    private String token;
    private Long parentRouteId;
    private String menuName;
    private int menuIndex;
    private boolean menuVisibility;

    public ApplicationPageImpl(final long applicationId, final long pageId, final String name) {
        super(name);
        this.applicationId = applicationId;
        this.pageId = pageId;
    }

    @Override
    public long getApplicationId() {
        return applicationId;
    }

    @Override
    public long getPageId() {
        return pageId;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public Long getParentRouteId() {
        return parentRouteId;
    }

    @Override
    public String getMenuName() {
        return menuName;
    }

    @Override
    public int getMenuIndex() {
        return menuIndex;
    }

    @Override
    public boolean getMenuVisibility() {
        return menuVisibility;
    }

}
