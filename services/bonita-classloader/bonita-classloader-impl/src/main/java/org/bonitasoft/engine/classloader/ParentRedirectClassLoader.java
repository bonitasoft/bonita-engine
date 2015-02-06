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
package org.bonitasoft.engine.classloader;

import java.io.InputStream;

public class ParentRedirectClassLoader extends ClassLoader {

    private static final String UNABLE_TO_FIND_THE_PARENT_CLASSLOADER_DYNAMICALLY = "Unable to find the parent classloader dynamically.";

	private final ParentClassLoaderResolver parentClassLoaderResolver;

	private final ClassLoaderService classLoaderService;

	private final String childClassLoaderType;

	private final long childClassLoaderId;

    public ParentRedirectClassLoader(final ClassLoader globalClassLoader, final ParentClassLoaderResolver parentClassLoaderResolver,
            final ClassLoaderService classLoaderService, final String childClassLoaderType, final long childClassLoaderId) {
		super(globalClassLoader);
		this.parentClassLoaderResolver = parentClassLoaderResolver;
		this.classLoaderService = classLoaderService;
		this.childClassLoaderType = childClassLoaderType;
		this.childClassLoaderId = childClassLoaderId;
	}

	@Override
	public Class<?> loadClass(final String name) throws ClassNotFoundException {
		try {
			return this.parentClassLoaderResolver.getParent(classLoaderService, childClassLoaderType, childClassLoaderId).loadClass(name);
		} catch (SClassLoaderException e) {
            throw new ClassNotFoundException(UNABLE_TO_FIND_THE_PARENT_CLASSLOADER_DYNAMICALLY, e);
		}
	}

	@Override
	public InputStream getResourceAsStream(final String name) {
		try {
			return this.parentClassLoaderResolver.getParent(classLoaderService, childClassLoaderType, childClassLoaderId).getResourceAsStream(name);
		} catch (SClassLoaderException e) {
            throw new RuntimeException(UNABLE_TO_FIND_THE_PARENT_CLASSLOADER_DYNAMICALLY, e);
		}
	}

}
