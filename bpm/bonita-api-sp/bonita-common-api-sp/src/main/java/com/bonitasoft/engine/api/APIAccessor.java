/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.api.CommandAPI;

/**
 * Gives access to Subscription edition specific APIs.
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 */
public interface APIAccessor extends org.bonitasoft.engine.api.APIAccessor {

    @Override
    IdentityAPI getIdentityAPI();

    @Override
    ProcessAPI getProcessAPI();

    @Override
    CommandAPI getCommandAPI();

    @Override
    ProfileAPI getProfileAPI();

    /**
     * @return the <code>MonitoringAPI</code>
     * @since 6.0
     */
    MonitoringAPI getMonitoringAPI();

    /**
     * @return the <code>PlatformMonitoringAPI</code>
     * @since 6.0
     */
    PlatformMonitoringAPI getPlatformMonitoringAPI();

    /**
     * @return the <code>LogAPI</code>
     * @since 6.0
     */
    LogAPI getLogAPI();

    /**
     * @return the <code>NodeAPI</code>
     * @since 6.1
     */
    NodeAPI getNodeAPI();

    /**
     * Gives access to ReportingAPI.
     *
     * @return the ReportingAPI, giving access to all reporting methods.
     */
    ReportingAPI getReportingAPI();

    /**
     * Gives access to ThemeAPI.
     *
     * @return The ThemeAPI, giving access to all theme methods.
     * @since 6.2
     */
    @Override
    ThemeAPI getThemeAPI();

    /**
     * Gives access to Page API
     *
     * @return
     * @since 6.3
     * @deprecated from version 7.0 on, use {@link #getCustomPageAPI()} instead.
     */
    @Deprecated
    PageAPI getPageAPI();

    /**
     * Gives access to {@link ApplicationAPI}
     *
     * @return
     * @since 6.4
     * @deprecated from version 7.0 on, use {@link #getLivingApplicationAPI()} instead.
     */
    @Deprecated
    ApplicationAPI getApplicationAPI();

}
