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

package org.bonitasoft.platform.setup.command;

import static org.bonitasoft.platform.setup.command.configure.DatabaseConfiguration.H2_DB_VENDOR;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.setup.command.configure.PropertyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 */
public class InitCommand extends PlatformSetupCommand {

    private final static Logger LOGGER = LoggerFactory.getLogger(InitCommand.class);

    public InitCommand() {
        super("init", "Initialise the database so that Bonita is ready to run with this database",
                CommandUtils.getFileContentFromClassPath("init_header.txt"),
                CommandUtils.getFileContentFromClassPath("init_footer.txt"));
    }

    @Override
    public void execute(Options options, CommandLine commandLine) throws PlatformException, CommandException {
        askConfirmationIfH2();
        getPlatformSetup(commandLine.getArgs()).init();
    }

    void askConfirmationIfH2() throws PlatformException, CommandException {
        final Properties properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream("/database.properties"));
            final PropertyReader propertyReader = new PropertyReader(properties);
            if (H2_DB_VENDOR.equals(propertyReader.getPropertyAndFailIfNull("db.vendor"))
                    && H2_DB_VENDOR.equals(propertyReader.getPropertyAndFailIfNull("bdm.db.vendor"))
                    && System.getProperty("h2.noconfirm") == null) {
                warn("Default H2 configuration detected. This is not recommended for production. If this is not the required configuration, change file 'database.properties' and run again.");
                System.out.print("Are you sure you want to continue? (y/n): ");
                final String answer = readAnswer();
                if (!"y".equalsIgnoreCase(answer)) {
                    throw new CommandException("Default H2 configuration not confirmed. Exiting.");
                }
            }
        } catch (IOException e) {
            throw new PlatformException("Error reading configuration file database.properties." +
                    " Please make sure the file is present at the root of the Platform Setup Tool folder, and that is has not been moved of deleted",
                    e);
        }
    }

    String readAnswer() throws IOException {
        final byte[] read = new byte[1];
        System.in.read(read);
        return new String(read);
    }

    void warn(String message) {
        LOGGER.warn(message);
    }
}
