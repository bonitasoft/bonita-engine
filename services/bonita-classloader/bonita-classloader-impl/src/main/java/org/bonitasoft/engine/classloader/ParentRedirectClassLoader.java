package org.bonitasoft.engine.classloader;

import java.io.InputStream;

public class ParentRedirectClassLoader extends ClassLoader {

	private final ParentClassLoaderResolver parentClassLoaderResolver;
	private final ClassLoaderService classLoaderService;
	private final String childClassLoaderType;
	private final long childClassLoaderId;

	public ParentRedirectClassLoader(final ClassLoader globalClassLoader, final ParentClassLoaderResolver parentClassLoaderResolver, final ClassLoaderService classLoaderService, final String childClassLoaderType, final long childClassLoaderId) {
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
		} catch (ClassLoaderException e) {
			throw new ClassNotFoundException("Unable to find the parent classloader dynamically", e);
		}
	}

	@Override
	public InputStream getResourceAsStream(final String name) {
		try {
			return this.parentClassLoaderResolver.getParent(classLoaderService, childClassLoaderType, childClassLoaderId).getResourceAsStream(name);
		} catch (ClassLoaderException e) {
			throw new RuntimeException("Unable to find the parent classloader dynamically", e);
		}
	}

}
