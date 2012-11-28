/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.businesslogger.model.SBusinessLog;
import org.bonitasoft.engine.platform.model.STenant;

import com.bonitasoft.engine.log.Log;
import com.bonitasoft.engine.log.LogBuilder;
import com.bonitasoft.engine.log.SeverityLevel;
import com.bonitasoft.engine.platform.Tenant;
import com.bonitasoft.engine.platform.TenantImpl;

/**
 * @author Matthieu Chaffotte
 */
public final class SPModelConvertor {

    public static List<Log> toLogs(final Collection<SBusinessLog> sBusinessLogs) {
        final List<Log> logs = new ArrayList<Log>();
        for (final SBusinessLog sBusinessLog : sBusinessLogs) {
            final Log log = toLog(sBusinessLog);
            logs.add(log);
        }
        return logs;
    }

    public static Log toLog(final SBusinessLog sBusinessLog) {
        final LogBuilder logBuilder = new LogBuilder().createNewInstance(sBusinessLog.getRawMessage(), sBusinessLog.getUserId(),
                new Date(sBusinessLog.getTimeStamp()));
        logBuilder.setLogId(sBusinessLog.getId());
        logBuilder.setActionType(sBusinessLog.getActionType());
        logBuilder.setActionScope(sBusinessLog.getActionScope());
        logBuilder.setCallerClassName(sBusinessLog.getCallerClassName());
        logBuilder.setCallerMethodName(sBusinessLog.getCallerMethodName());
        logBuilder.setSeverity(SeverityLevel.valueOf(sBusinessLog.getSeverity().name()));
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

    public static List<Tenant> toTenants(final List<STenant> sTenants) {
        final List<Tenant> tenants = new ArrayList<Tenant>();
        for (final STenant sTenant : sTenants) {
            tenants.add(toTenant(sTenant));
        }
        return tenants;
    }

}
