/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.PlatformNotStartedException;
import org.bonitasoft.engine.exception.StopNodeException;

import com.bonitasoft.engine.api.impl.PlatformAPIExt;
import com.bonitasoft.engine.exception.TenantDeactivationException;
import com.bonitasoft.engine.exception.TenantNotFoundException;
import com.bonitasoft.engine.platform.Tenant;
import com.bonitasoft.manager.Manager;

/**
 * @author Matthieu Chaffotte
 */
public class LicenceChecker {

    private final Timer timer;

    private LicenceChecker() {
        timer = new Timer(false);
        timer.scheduleAtFixedRate(new TimerTasker(), new Date(), 1000);
    }

    private static class LicenceCheckerHolder {

        public static final LicenceChecker INSTANCE = new LicenceChecker();

    }

    public static LicenceChecker getInstance() {
        return LicenceCheckerHolder.INSTANCE;
    }

    public void checkLicence() {
        timer.cancel();
        timer.purge();
        timer.scheduleAtFixedRate(new TimerTasker(), new Date(), 1000);
    }

    private static class TimerTasker extends TimerTask {

        @Override
        public void run() {
            if (!Manager.isValid()) {
                final PlatformAPIExt platformAPI = new PlatformAPIExt();
                try {
                    final List<Tenant> tenants = platformAPI.getTenants(0, 10);
                    for (final Tenant tenant : tenants) {
                        platformAPI.deactiveTenant(tenant.getId());
                    }
                    platformAPI.stopNode();
                } catch (final InvalidSessionException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (final PlatformNotStartedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (final TenantNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (final TenantDeactivationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (final StopNodeException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    cancel();
                }
            }
        }
    }

}
