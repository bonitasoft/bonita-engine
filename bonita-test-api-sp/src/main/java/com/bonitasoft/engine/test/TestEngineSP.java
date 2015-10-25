package com.bonitasoft.engine.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.platform.Tenant;
import com.bonitasoft.engine.platform.TenantDeactivationException;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.TestEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 */
public class TestEngineSP extends TestEngine {


    private long defaultTenantId;
    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineSP.class.getName());

    private static TestEngineSP INSTANCE = new TestEngineSP();

    public static TestEngineSP getInstance() {
        return INSTANCE;
    }

    protected TestEngineSP() {
        super();
        replaceInstance(this);
    }

    @Override
    protected synchronized void doStart() throws Exception {
        LOGGER.info("Starting SP version of the engine");
        super.doStart();
    }

    @Override
    public void deleteTenantAndPlatform() throws BonitaException {
        destroyPlatformAndTenants();
        deletePlatformStructure();
    }

    @Override
    public void initPlatformAndTenant() throws Exception {
        try {
            createPlatformStructure();
        } catch (final Exception e) {
            final Logger logger = LoggerFactory.getLogger(TestEngineSP.class);
            logger.error("unable to create platform", e);
            final PlatformSession session = loginOnPlatform();
            final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
            platformAPI.stopNode();
            platformAPI.cleanPlatform();
            deletePlatformStructure();
            createPlatformStructure();
        }

        createEnvironmentWithDefaultTenant();
    }

    public void createEnvironmentWithDefaultTenant() throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.initializePlatform();
        platformAPI.startNode();
        defaultTenantId = platformAPI.getDefaultTenant().getId();
        logoutOnPlatform(session);
        deployCommandsOnDefaultTenant();
    }

    public void destroyPlatformAndTenants() throws BonitaException {
        undeployCommands();
        final PlatformSession platformSession = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        List<Tenant> tenants;
        try {
            tenants = platformAPI.searchTenants(new SearchOptionsBuilder(0, 1000).done()).getResult();
        } catch (final SearchException e) {
            platformAPI.startNode();
            tenants = platformAPI.searchTenants(new SearchOptionsBuilder(0, 1000).done()).getResult();
        }
        for (final Tenant tenant : tenants) {
            try {
                if (!tenant.isDefaultTenant()) {
                    platformAPI.deactiveTenant(tenant.getId());
                }
            } catch (final TenantDeactivationException tde) {
                // Nothing to do
            }
            if (!tenant.isDefaultTenant()) {
                platformAPI.deleteTenant(tenant.getId());
            }
        }
        platformAPI.stopNode();
        platformAPI.cleanPlatform();
        logoutOnPlatform(platformSession);
    }

    public long getDefaultTenantId() {
        return defaultTenantId;
    }

    @Override
    protected String prepareBonitaHome() throws IOException {
        final String bonitaHome = super.prepareBonitaHome();
        final File licFolder = new File(System.getProperty("user.home"), "lic");
        final File toDir = new File(new File(bonitaHome, "server"), "licenses");
        toDir.mkdirs();
        final File[] files = licFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".lic");
            }
        });
        for (File file : files) {
            try (FileInputStream fis = new FileInputStream(file); FileOutputStream fos = new FileOutputStream(new File(toDir, file.getName()))) {
                IOUtils.copy(fis, fos);
            }
        }
        return bonitaHome;
    }
}
