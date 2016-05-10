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

package org.bonitasoft.engine.dependency.impl;

import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.service.InjectedService;

/**
 * @author Baptiste Mesta
 */
public class RefreshPlatformClassLoaderTask extends AbstractRefreshClassLoaderTask {


    private transient DependencyService dependencyService;

    public RefreshPlatformClassLoaderTask(ScopeType scopeType, long id) {
        super(id, scopeType);
    }

    @InjectedService
    public void setPlatformDependencyService(DependencyService dependencyService){
        this.dependencyService = dependencyService;
    }

    @Override
    public DependencyService getDependencyService() {
        return dependencyService;
    }
}
