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
package org.bonitasoft.engine.dependency.impl;

import javax.annotation.PostConstruct;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.springframework.stereotype.Component;

@Component
public class DependencyServiceRegistrator {

    private final TenantDependencyService tenantDependencyService;
    private final ClassLoaderService classLoaderService;

    public DependencyServiceRegistrator(TenantDependencyService tenantDependencyService,
            ClassLoaderService classLoaderService) {
        this.tenantDependencyService = tenantDependencyService;
        this.classLoaderService = classLoaderService;
    }

    @PostConstruct
    private void registerDependencyService() {
        classLoaderService.registerDependencyService(tenantDependencyService);
    }
}
