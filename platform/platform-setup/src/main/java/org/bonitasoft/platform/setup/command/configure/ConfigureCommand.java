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

package org.bonitasoft.platform.setup.command.configure;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.setup.command.CommandUtils;
import org.bonitasoft.platform.setup.command.PlatformSetupCommand;

/**
 * @author Baptiste Mesta
 */
public class ConfigureCommand extends PlatformSetupCommand {

    public ConfigureCommand() {
        super("configure",
                "Configure a Bonita bundle to use your specific database configuration (defined in database.properties or via command line parameters)",
                CommandUtils.getFileContentFromClassPath("configure_header.txt"), CommandUtils.getFileContentFromClassPath("configure_footer.txt"));
    }

    @Override
    public void execute(Options options, CommandLine commandLine) throws PlatformException {
        BundleConfigurator bundleConfigurator = createBundleResolver().getConfigurator();
        if (bundleConfigurator != null) {
            bundleConfigurator.configureApplicationServer();
        }
    }

    BundleResolver createBundleResolver() {
        return new BundleResolver();
    }
}
