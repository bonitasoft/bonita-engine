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
package org.bonitasoft.engine.api.impl.transaction.platform;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class RefreshPlatformClassLoader implements Callable<Object> {

    private final PlatformServiceAccessor platformAccessor;

    public RefreshPlatformClassLoader(final PlatformServiceAccessor platformAccessor) {
        this.platformAccessor = platformAccessor;
    }

    @Override
    public Object call() throws SBonitaException {
        final DependencyService platformDependencyService = platformAccessor.getDependencyService();
        final ClassLoaderService classLoaderService = platformAccessor.getClassLoaderService();
        platformDependencyService.refreshClassLoader(ScopeType.valueOf(classLoaderService.getGlobalClassLoaderType()),
                classLoaderService.getGlobalClassLoaderId());
        final PlatformService platformService = platformAccessor.getPlatformService();
        // reput the platform in cache at the node start
        platformService.getPlatform();
        return null;
    }

}
