/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.impl.transaction.platform.GetTenantInstance;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.TenantManagementAPI;
import com.bonitasoft.engine.api.TenantMode;
import com.bonitasoft.engine.api.impl.transaction.UpdateTenant;
import com.bonitasoft.engine.bdm.BDMCodeGenerator;
import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import com.bonitasoft.engine.businessdata.BusinessDataRepositoryDeploymentException;
import com.bonitasoft.engine.businessdata.InvalidBusinessDataModelException;
import com.bonitasoft.engine.io.IOUtils;
import com.bonitasoft.engine.platform.TenantNotFoundException;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

public class TenantManagementAPIExt implements TenantManagementAPI {

	private static TenantServiceAccessor getTenantAccessor() {
		try {
			final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
			final long tenantId = sessionAccessor.getTenantId();
			return TenantServiceSingleton.getInstance(tenantId);
		} catch (final Exception e) {
			throw new BonitaRuntimeException(e);
		}
	}

	@Override
	@AvailableOnMaintenanceTenant
	public void deployBusinessDataRepository(final byte[] zip) throws InvalidBusinessDataModelException, BusinessDataRepositoryDeploymentException {
		final TenantServiceAccessor tenantAccessor = getTenantAccessor();
		try {
			// TODO: should be in activate tenant
			final BusinessDataRepository bdr = tenantAccessor.getBusinessDataRepository();
			bdr.deploy(buildBDMJAR(zip), tenantAccessor.getTenantId());
			bdr.start();
		} catch (final IllegalStateException e) {
			throw new InvalidBusinessDataModelException(e);
		} catch (final SBusinessDataRepositoryDeploymentException e) {
			throw new BusinessDataRepositoryDeploymentException(e);
		}
	}

	private byte[] buildBDMJAR(final byte[] zip) throws BusinessDataRepositoryDeploymentException {
		final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
		try {
			final BusinessObjectModel bom = converter.unzip(zip);
			final BDMCodeGenerator codeGenerator = new BDMCodeGenerator(bom);
			final File TmpBDMDirectory = File.createTempFile("bdm", null);
			TmpBDMDirectory.delete();
			TmpBDMDirectory.mkdir();

			codeGenerator.generate(TmpBDMDirectory);
			final Collection<File> files = FileUtils.listFiles(TmpBDMDirectory, new String[] { "java" }, true);
			final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
			final Iterable<? extends JavaFileObject> compUnits = fileManager.getJavaFileObjectsFromFiles(files);

			List<String> options = new ArrayList<String>();
			options.add("-classpath");
			StringBuilder sb = new StringBuilder();
			URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
			URL resource = urlClassLoader.getResource(Entity.class.getName());
			if(resource == null){
				throw new CreationException(Entity.class.getName() +" not found in classloader for bdm compilation");
			}
			sb.append(resource.getFile()).append(File.pathSeparator);
			options.add(sb.toString());
			final Boolean compiled = compiler.getTask(null, fileManager, null, options, null, compUnits).call();
			if (!compiled) {
				throw new CreationException("The compilation process fails");
			}
			final byte[] jar = IOUtils.toJar(TmpBDMDirectory.getAbsolutePath());
			FileUtils.deleteDirectory(TmpBDMDirectory);
			return jar;
		} catch (final Exception e) {
			throw new BusinessDataRepositoryDeploymentException(e);
		}
	}

	@Override
	@AvailableOnMaintenanceTenant
	public boolean isTenantInMaintenance(final long tenantId) throws TenantNotFoundException {
		final GetTenantInstance getTenant = new GetTenantInstance(tenantId, getPlatformService());
		try {
			getTenant.execute();
			return getTenant.getResult().isInMaintenance();
		} catch (final STenantNotFoundException e) {
			throw new TenantNotFoundException("No tenant exists with id: " + tenantId);
		} catch (final SBonitaException e) {
			throw new RetrieveException("Unable to retrieve the tenant with id " + tenantId);
		}
	}

	@Override
	@AvailableOnMaintenanceTenant
	public void setTenantMaintenanceMode(final long tenantId, final TenantMode mode) throws UpdateException {
		final PlatformService platformService = getPlatformService();

		final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
		final STenantBuilderFactory tenantBuilderFact = BuilderFactory.get(STenantBuilderFactory.class);
		switch (mode) {
		case AVAILABLE:
			descriptor.addField(tenantBuilderFact.getInMaintenanceKey(), STenantBuilderFactory.AVAILABLE);
			break;
		case MAINTENANCE:
			descriptor.addField(tenantBuilderFact.getInMaintenanceKey(), STenantBuilderFactory.IN_MAINTENANCE);
			break;
		default:
			break;
		}
		updateTenantFromId(tenantId, platformService, descriptor);
	}

	protected PlatformService getPlatformService() {
		final PlatformServiceAccessor platformAccessor = getPlatformAccessorNoException();
		final PlatformService platformService = platformAccessor.getPlatformService();
		return platformService;
	}

	protected PlatformServiceAccessor getPlatformAccessorNoException() {
		try {
			return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
		} catch (final Exception e) {
			throw new BonitaRuntimeException(e);
		}
	}

	protected void updateTenantFromId(final long tenantId, final PlatformService platformService, final EntityUpdateDescriptor descriptor)
			throws UpdateException {
		try {
			new UpdateTenant(tenantId, descriptor, platformService).execute();
		} catch (final SBonitaException e) {
			throw new UpdateException("Could not update the tenant maintenance mode", e);
		}
	}

}
