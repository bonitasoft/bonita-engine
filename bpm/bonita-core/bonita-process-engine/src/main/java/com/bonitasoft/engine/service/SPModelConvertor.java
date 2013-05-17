/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.service.ModelConvertor;

import com.bonitasoft.engine.bpm.model.breakpoint.Breakpoint;
import com.bonitasoft.engine.bpm.model.breakpoint.impl.BreakpointImpl;
import com.bonitasoft.engine.core.process.instance.model.breakpoint.SBreakpoint;
import com.bonitasoft.engine.log.Log;
import com.bonitasoft.engine.log.LogBuilder;
import com.bonitasoft.engine.log.SeverityLevel;
import com.bonitasoft.engine.platform.Tenant;
import com.bonitasoft.engine.platform.TenantCreator;
import com.bonitasoft.engine.platform.TenantCreator.TenantField;
import com.bonitasoft.engine.platform.TenantImpl;

/**
 * @author Matthieu Chaffotte
 */
public final class SPModelConvertor extends ModelConvertor {

    private static final String PLATFORM_STATUS_DEACTIVATED = "DEACTIVATED";

    public static List<Log> toLogs(final Collection<SQueriableLog> sQueriableLogs) {
        final List<Log> logs = new ArrayList<Log>();
        for (final SQueriableLog sQueriableLog : sQueriableLogs) {
            final Log log = toLog(sQueriableLog);
            logs.add(log);
        }
        return logs;
    }

    public static Log toLog(final SQueriableLog sQueriableLog) {
        final LogBuilder logBuilder = new LogBuilder().createNewInstance(sQueriableLog.getRawMessage(), sQueriableLog.getUserId(),
                new Date(sQueriableLog.getTimeStamp()));
        logBuilder.setLogId(sQueriableLog.getId());
        logBuilder.setActionType(sQueriableLog.getActionType());
        logBuilder.setActionScope(sQueriableLog.getActionScope());
        logBuilder.setCallerClassName(sQueriableLog.getCallerClassName());
        logBuilder.setCallerMethodName(sQueriableLog.getCallerMethodName());
        logBuilder.setSeverity(SeverityLevel.valueOf(sQueriableLog.getSeverity().name()));
        return logBuilder.done();
    }

    public static Tenant toTenant(final STenant sTenant) {
        final TenantImpl tenant = new TenantImpl();
        tenant.setTenantId(sTenant.getId());
        tenant.setName(sTenant.getName());
        tenant.setState(sTenant.getStatus());
        tenant.setIconPath(sTenant.getIconPath());
        tenant.setIconName(sTenant.getIconName());
        tenant.setDescription(sTenant.getDescription());
        tenant.setDefaultTenant(sTenant.isDefaultTenant());
        tenant.setCreationDate(new Date(sTenant.getCreated()));
        // no createdBy in tenantImpl
        return tenant;
    }

    public static STenant constructTenant(final TenantCreator tCreator, final STenantBuilder sTenantBuilder) {
        final Map<TenantField, Serializable> fields = tCreator.getFields();
        sTenantBuilder.createNewInstance((String) fields.get(TenantField.NAME), "defaultUser", System.currentTimeMillis(), PLATFORM_STATUS_DEACTIVATED,
                (Boolean) fields.get(TenantField.DEFAULT_TENANT));
        sTenantBuilder.setDescription((String) fields.get(TenantField.DESCRIPTION));
        sTenantBuilder.setIconName((String) fields.get(TenantField.ICON_NAME));
        sTenantBuilder.setIconPath((String) fields.get(TenantField.ICON_PATH));
        return sTenantBuilder.done();
    }

    public static List<Tenant> toTenants(final List<STenant> sTenants) {
        final List<Tenant> tenants = new ArrayList<Tenant>();
        for (final STenant sTenant : sTenants) {
            tenants.add(toTenant(sTenant));
        }
        return tenants;
    }

    public static Breakpoint toBreakpoint(final SBreakpoint sBreakpoint) {
        final BreakpointImpl breakpoint = new BreakpointImpl();
        breakpoint.setId(sBreakpoint.getId());
        breakpoint.setDefinitionId(sBreakpoint.getDefinitionId());
        breakpoint.setInstanceId(sBreakpoint.getInstanceId());
        breakpoint.setElementName(sBreakpoint.getElementName());
        breakpoint.setInstanceScope(sBreakpoint.isInstanceScope());
        breakpoint.setInterruptedStateId(sBreakpoint.getInterruptedStateId());
        breakpoint.setStateId(sBreakpoint.getStateId());
        return breakpoint;
    }

    public static List<Breakpoint> toBreakpoints(final List<SBreakpoint> sBreakpoints) {
        final List<Breakpoint> breakpoints = new ArrayList<Breakpoint>(sBreakpoints.size());
        for (final SBreakpoint sBreakpoint : sBreakpoints) {
            breakpoints.add(toBreakpoint(sBreakpoint));
        }
        return breakpoints;
    }

}
