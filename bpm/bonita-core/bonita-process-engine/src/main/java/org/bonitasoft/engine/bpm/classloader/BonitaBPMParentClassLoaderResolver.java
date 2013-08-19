package org.bonitasoft.engine.bpm.classloader;

import org.bonitasoft.engine.classloader.ClassLoaderException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.ParentClassLoaderResolver;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

public class BonitaBPMParentClassLoaderResolver implements ParentClassLoaderResolver {

	private final ReadSessionAccessor sessionAccessor;
	
	public BonitaBPMParentClassLoaderResolver(final ReadSessionAccessor sessionAccessor) {
	    this.sessionAccessor = sessionAccessor;
    }
	
	@Override
	public ClassLoader getParent(final ClassLoaderService classLoaderService, final String childClassLoaderType, final long childClassLoaderId) throws ClassLoaderException {
		if ("process".equals(childClassLoaderType)) {
			try {
			final Long tenantId = this.sessionAccessor.getTenantId();
				return classLoaderService.getLocalClassLoader("tenant", tenantId);
			} catch (TenantIdNotSetException e) {
				return classLoaderService.getGlobalClassLoader();
			}
		} else if ("tenant".equals(childClassLoaderType)) {
			return classLoaderService.getGlobalClassLoader();
		} else if ("___datasource___".equals(childClassLoaderType)) {
			return classLoaderService.getGlobalClassLoader();
		}
		throw new BonitaRuntimeException("unable to find a parent for type: " + childClassLoaderType); 
	}
}