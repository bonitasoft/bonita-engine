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

import java.io.Serializable;

import com.bonitasoft.engine.service.BroadCastedTask;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.service.InjectedService;

/**
 * @author Baptiste Mesta
 */
public class RefreshClassLoaderTask implements Serializable, BroadCastedTask<Void> {

    private transient DependencyService dependencyService;
    private ScopeType scopeType;
    private long id;
    private String nodeName;

    public RefreshClassLoaderTask(ScopeType scopeType, long id) {
        this.scopeType = scopeType;
        this.id = id;
    }

    @Override
    public Void call() throws Exception {
        dependencyService.refreshClassLoader(scopeType, id);
        return null;
    }

    @InjectedService
    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    @Override
    public void setName(String nodeName) {
        this.nodeName = nodeName;
    }
}
