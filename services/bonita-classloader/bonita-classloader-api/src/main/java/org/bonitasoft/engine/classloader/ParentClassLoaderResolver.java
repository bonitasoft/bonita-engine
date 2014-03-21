package org.bonitasoft.engine.classloader;

public interface ParentClassLoaderResolver {

	ClassLoader getParent(final ClassLoaderService classLoaderService, final String childClassLoaderType, final long childClassLoaderId) throws SClassLoaderException;
}
