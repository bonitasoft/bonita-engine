/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 */
package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.impl.ProcessConfigurationAPIImpl;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.FormMappingNotFoundException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.ProcessConfigurationAPI;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;
import com.bonitasoft.manager.Features;

/**
 * author Emmanuel Duchastenier
 */
public class ProcessConfigurationAPIExt extends ProcessConfigurationAPIImpl implements ProcessConfigurationAPI {

    protected LicenseChecker getLicenseChecker() {
        return LicenseChecker.getInstance();
    }

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public FormMapping updateFormMapping(final long formMappingId, final String url, Long pageId) throws FormMappingNotFoundException, UpdateException {
        getLicenseChecker().checkLicenseAndFeature(Features.LIVE_UPDATE_FORM_MAPPING);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final FormMappingService formMappingService = tenantAccessor.getFormMappingService();
        try {
            SFormMapping sFormMapping = formMappingService.get(formMappingId);
            formMappingService.update(sFormMapping, url, pageId);
            tenantAccessor.getDependencyResolver().resolveDependencies(sFormMapping.getProcessDefinitionId(), tenantAccessor);
            return ModelConvertor.toFormMapping(sFormMapping);
        } catch (SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (SObjectNotFoundException e) {
            throw new FormMappingNotFoundException("Unable to find the form mapping with id " + formMappingId);
        } catch (SObjectModificationException e) {
            throw new UpdateException("Unable to update the form mapping " + formMappingId, e);
        }
    }

    @Override
    public void updateExpressionContent(long processDefinitionId, long expressionDefinitionId, String content) {
        // TODO implement me !
    }

}
