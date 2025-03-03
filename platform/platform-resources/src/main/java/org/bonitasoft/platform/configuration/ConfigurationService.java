/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.platform.configuration;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;
import org.bonitasoft.platform.configuration.model.LightBonitaConfiguration;
import org.bonitasoft.platform.exception.PlatformException;

/**
 * Give access to Bonita Platform configuration.
 * Is used by setup mechanism to retrieve configuration before running the Engine + Portal, on a system that does give
 * access to a persistent filesystem.
 *
 * @author Emmanuel Duchastenier
 */
public interface ConfigurationService {

    /**
     * Retrieves the portal configuration at platform-level.
     *
     * @return a list of BonitaConfiguration that represents each file
     */
    List<BonitaConfiguration> getPlatformPortalConf();

    /**
     * Retrieves the platform configuration at platform level.
     *
     * @return a list of BonitaConfiguration that represents each file
     */
    List<BonitaConfiguration> getPlatformEngineConf();

    /**
     * Retrieves the engine tenant configuration for a tenant
     *
     * @return a list of BonitaConfiguration that represents each file
     */

    List<BonitaConfiguration> getTenantEngineConf();

    /**
     * Retrieves the security scripts for a tenant
     *
     * @return a list of BonitaConfiguration that represents each file
     */

    List<BonitaConfiguration> getTenantSecurityScripts();

    /**
     * store security script for a tenant
     *
     * @param bonitaConfigurations list of files
     */
    void storeTenantSecurityScripts(List<BonitaConfiguration> bonitaConfigurations);

    /**
     * store tenant configuration files for portal
     *
     * @param bonitaConfigurations list of files
     */
    void storeTenantPortalConf(List<BonitaConfiguration> bonitaConfigurations);

    /**
     * updates tenant configurations for portal, for all tenants and for tenant template.
     *
     * @param bonitaConfigurations list of configurations to store
     */
    void updateTenantPortalConf(List<BonitaConfiguration> bonitaConfigurations);

    void updateDefaultConfiguration(Path configurationRootFolder) throws PlatformException;

    /**
     * Retrieves the portal configuration for a tenant
     *
     * @return list of files
     */
    List<BonitaConfiguration> getTenantPortalConf();

    /**
     * Retrieves a portal configuration file for a tenant
     */
    BonitaConfiguration getTenantPortalConfiguration(String file);

    /**
     * store platform configuration file in database
     *
     * @param bonitaConfigurations list of files
     */
    void storePlatformEngineConf(List<BonitaConfiguration> bonitaConfigurations);

    /**
     * store platform configuration files for engine
     *
     * @param configurationRootFolder root folder containing configuration files
     */
    void storePlatformConfiguration(File configurationRootFolder) throws PlatformException;

    /**
     * store whole configuration files for engine and portal, excluding licenses files
     *
     * @param configurationRootFolder path to root folder
     */
    void storeAllConfiguration(Path configurationRootFolder) throws PlatformException;

    /**
     * write all configuration files
     * directory structure :
     * .
     * ├── platform_engine
     * ├── platform_portal
     * ├── tenant_engine
     * ├── tenant_portal
     * └── tenant_security_scripts
     */
    List<File> writeAllConfigurationToFolder(File configurationFolder, File licenseFolder) throws PlatformException;

    /**
     * read licensesFolder for license files
     * sub-folders are ignored
     * each *.lic file is stored in database
     */
    void storeLicenses(File licensesFolder) throws PlatformException;

    /**
     * Retrieves all license files stored in database.
     *
     * @return a list of BonitaConfiguration that represents each license file
     */
    List<BonitaConfiguration> getLicenses() throws PlatformException;

    /**
     * Delete all configuration and license files
     */
    void deleteAllConfiguration();

    List<LightBonitaConfiguration> getMandatoryStructureConfiguration();

    void storeConfigurationsIfNotExist(List<FullBonitaConfiguration> configurations);
}
