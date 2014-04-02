/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.actor.xml.GroupPathsBinding;
import org.bonitasoft.engine.actor.xml.RoleNamesBinding;
import org.bonitasoft.engine.actor.xml.UserNamesBinding;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.profile.xml.ChildrenEntriesBinding;
import org.bonitasoft.engine.profile.xml.MembershipBinding;
import org.bonitasoft.engine.profile.xml.MembershipsBinding;
import org.bonitasoft.engine.profile.xml.ParentProfileEntryBinding;
import org.bonitasoft.engine.profile.xml.ProfileEntriesBinding;
import org.bonitasoft.engine.profile.xml.ProfileEntryBinding;
import org.bonitasoft.engine.profile.xml.ProfileMappingBinding;
import org.bonitasoft.engine.profile.xml.ProfilesBinding;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.SInvalidSchemaException;

import com.bonitasoft.engine.core.process.instance.api.BreakpointService;
import com.bonitasoft.engine.core.reporting.ReportingService;
import com.bonitasoft.engine.monitoring.TenantMonitoringService;
import com.bonitasoft.engine.parameter.ParameterService;
import com.bonitasoft.engine.profile.xml.ProfileBinding;
import com.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SpringTenantServiceAccessor extends org.bonitasoft.engine.service.impl.SpringTenantServiceAccessor implements TenantServiceAccessor {

    private ParameterService parameterService;

    private BreakpointService breakpointService;

    private ReportingService reportingService;

    private TenantMonitoringService tenantMonitoringServie;

    private SearchEntitiesDescriptor searchEntitiesDescriptor;

    public SpringTenantServiceAccessor(final Long tenantId) {
        super(tenantId);
    }

    @Override
    public ParameterService getParameterService() {
        if (parameterService == null) {
            parameterService = lookupService(ParameterService.class);
        }
        return parameterService;
    }

    @Override
    public BreakpointService getBreakpointService() {
        if (breakpointService == null) {
            breakpointService = lookupService(BreakpointService.class);
        }
        return breakpointService;
    }

    @Override
    public SearchEntitiesDescriptor getSearchEntitiesDescriptor() {
        if (searchEntitiesDescriptor == null) {
            searchEntitiesDescriptor = lookupService(SearchEntitiesDescriptor.class);
        }
        return searchEntitiesDescriptor;
    }

    @Override
    public ReportingService getReportingService() {
        if (reportingService == null) {
            reportingService = lookupService(ReportingService.class);
        }
        return reportingService;
    }

    @Override
    public TenantMonitoringService getTenantMonitoringService() {
        if (tenantMonitoringServie == null) {
            tenantMonitoringServie = lookupService(TenantMonitoringService.class);
        }
        return tenantMonitoringServie;
    }

    private <T> T lookupService(final Class<T> clazz) {
        return getBeanAccessor().getService(clazz);
    }

    @Override
    public Parser getProfileParser() {
        final List<Class<? extends ElementBinding>> bindings = new ArrayList<Class<? extends ElementBinding>>();
        bindings.add(ProfileBinding.class);
        bindings.add(ProfilesBinding.class);
        bindings.add(ProfileEntryBinding.class);
        bindings.add(ParentProfileEntryBinding.class);
        bindings.add(ChildrenEntriesBinding.class);
        bindings.add(ProfileEntriesBinding.class);
        bindings.add(ProfileMappingBinding.class);
        bindings.add(MembershipsBinding.class);
        bindings.add(MembershipBinding.class);
        bindings.add(UserNamesBinding.class);
        bindings.add(RoleNamesBinding.class);
        bindings.add(RoleNamesBinding.class);
        bindings.add(GroupPathsBinding.class);
        final Parser parser = getParserFactgory().createParser(bindings);
        final InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("profiles.xsd");
        try {
            parser.setSchema(resource);
            return parser;
        } catch (final SInvalidSchemaException ise) {
            throw new BonitaRuntimeException(ise);
        } finally {
            try {
                if (resource != null) {
                    resource.close();
                }
            } catch (final IOException ioe) {
                throw new BonitaRuntimeException(ioe);
            }
        }
    }

}
