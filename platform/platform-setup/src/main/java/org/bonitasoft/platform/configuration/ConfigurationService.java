/**
 * Copyright (C) 2016 Bonitasoft S.A.
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
import org.bonitasoft.platform.configuration.model.LightBonitaConfiguration;
import org.bonitasoft.platform.exception.PlatformException;

/**
 * Give access to Bonita Platform configuration.
 * Is used by setup mechanism to retrieve configuration before running the Engine + Portal, on a system that does give access to a persistent filesystem.
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
     * Retrieves the platform configuration at platform-init level.
     *
     * @return a list of BonitaConfiguration that represents each file
     */
    List<BonitaConfiguration> getPlatformInitEngineConf();

    /**
     * Retrieves the platform configuration at platform level.
     *
     * @return a list of BonitaConfiguration that represents each file
     */
    List<BonitaConfiguration> getPlatformEngineConf();

    /**
     * Retrieves the platform configuration for tenant template
     *
     * @return a list of BonitaConfiguration that represents each file
     */
    List<BonitaConfiguration> getTenantTemplateEngineConf();

    /**
     * Retrieves the security script configuration for tenant template
     *
     * @return a list of BonitaConfiguration that represents each file
     */
    List<BonitaConfiguration> getTenantTemplateSecurityScripts();

    /**
     * Retrieves the engine tenant configuration for a tenant
     *
     * @param tenantId tenant identifier
     * @return a list of BonitaConfiguration that represents each file
     */

    List<BonitaConfiguration> getTenantEngineConf(long tenantId);

    /**
     * Retrieves the security scripts for a tenant
     *
     * @param tenantId tenant identifier
     * @return a list of BonitaConfiguration that represents each file
     */

    List<BonitaConfiguration> getTenantSecurityScripts(long tenantId);

    /**
     * store platform init configuration file in database
     * 
     * @param bonitaConfigurations list of files
     */
    void storePlatformInitEngineConf(List<BonitaConfiguration> bonitaConfigurations);

    /**
     * store platform configuration file in database
     * 
     * @param bonitaConfigurations list of files
     */
    void storePlatformEngineConf(List<BonitaConfiguration> bonitaConfigurations);

    /**
     * store tenant template configuration file in database
     *
     * @param bonitaConfigurations list of files
     */
    void storeTenantTemplateEngineConf(List<BonitaConfiguration> bonitaConfigurations);

    /**
     * store tenant template security scripts
     *
     * @param bonitaConfigurations list of files
     */
    void storeTenantTemplateSecurityScripts(List<BonitaConfiguration> bonitaConfigurations);

    /**
     * store tenant configuration file in database
     *
     * @param bonitaConfigurations list of files
     * @param tenantId tenant identifier
     */
    void storeTenantEngineConf(List<BonitaConfiguration> bonitaConfigurations, long tenantId);

    /**
     * store security script for a tenant
     *
     * @param bonitaConfigurations list of files
     * @param tenantId tenant identifier
     */
    void storeTenantSecurityScripts(List<BonitaConfiguration> bonitaConfigurations, long tenantId);

    /**
     * store tenant template configuration files for portal
     *
     * @param bonitaConfigurations list of files
     */
    void storeTenantTemplatePortalConf(List<BonitaConfiguration> bonitaConfigurations);

    /**
     * store tenant configuration files for portal
     *
     * @param bonitaConfigurations list of files
     * @param tenantId
     */
    void storeTenantPortalConf(List<BonitaConfiguration> bonitaConfigurations, long tenantId);

    /**
     * updates tenant configurations for portal, for all tenants and for tenant template.
     *
     * @param bonitaConfigurations list of configurations to store
     */
    void updateTenantPortalConfForAllTenantsAndTemplate(List<BonitaConfiguration> bonitaConfigurations);

    void updateDefaultConfigurationForAllTenantsAndTemplate(Path configurationRootFolder) throws PlatformException;

    /**
     * store platform configuration files for portal
     *
     * @param bonitaConfigurations list of files
     */
    void storePlatformPortalConf(List<BonitaConfiguration> bonitaConfigurations);

    /**
     * Retrieves the portal template configuration for a tenant
     *
     * @return list of files
     */
    List<BonitaConfiguration> getTenantTemplatePortalConf();

    /**
     * Retrieves the portal configuration for a tenant
     *
     * @param tenantId
     * @return list of files
     */
    List<BonitaConfiguration> getTenantPortalConf(long tenantId);

    /**
     * Retrieves a portal configuration file for a tenant
     *
     * @param tenantId
     * @param file
     * @return file
     */
    BonitaConfiguration getTenantPortalConfiguration(long tenantId, String file);

    /**
     * Read configuration files located under configuration root folder
     * each file is stored in database
     *
     * @param configurationRootFolder root folder containing configuration files
     * @param tenantId tenant key
     * @throws PlatformException
     */
    void storeTenantConfiguration(File configurationRootFolder, long tenantId) throws PlatformException;

    /**
     * store platform configuration files for engine
     *
     * @param configurationRootFolder root folder containing configuration files
     * @throws PlatformException
     */
    void storePlatformConfiguration(File configurationRootFolder) throws PlatformException;

    /**
     * store whole configuration files for engine and portal, excluding licenses files
     *
     * @param configurationRootFolder path to root folder
     * @throws PlatformException
     */
    void storeAllConfiguration(Path configurationRootFolder) throws PlatformException;

    /**
     * write all configuration files
     * directory structure :
     * .
     * ├── platform_engine
     * ├── platform_init_engine
     * ├── platform_portal
     * ├── tenant_template_engine
     * ├── tenant_template_portal
     * └── tenant_template_security_scripts
     *
     * @param configurationFolder
     * @param licenseFolder
     * @throws PlatformException
     */
    List<File> writeAllConfigurationToFolder(File configurationFolder, File licenseFolder) throws PlatformException;

    /**
     * read licensesFolder for license files
     * sub-folders are ignored
     * each *.lic file is stored in database
     *
     * @param licensesFolder
     * @throws PlatformException
     */
    void storeLicenses(File licensesFolder) throws PlatformException;

    /**
     * Retrieves all license files stored in database.
     *
     * @return a list of BonitaConfiguration that represents each license file
     * @throws PlatformException
     */
    List<BonitaConfiguration> getLicenses() throws PlatformException;

    /**
     * Delete all configuration files for a tenant
     *
     * @param tenantId the tenant id.
     * @throws IllegalArgumentException when tenantId value is out of range (<= 0 )
     */
    void deleteTenantConfiguration(long tenantId);

    /**
     * Delete all configuration and license files
     */
    void deleteAllConfiguration();

    List<LightBonitaConfiguration> getMandatoryStructureConfiguration();
}
