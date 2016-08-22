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
package org.bonitasoft.platform.setup;

import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.setup.jndi.MemoryJNDISetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Emmanuel Duchastenier
 */
@SpringBootApplication
@ComponentScan(basePackages = { "org.bonitasoft.platform.setup", "org.bonitasoft.platform.configuration", "org.bonitasoft.platform.version" })
public class PlatformSetupApplication {

    private final static Logger LOGGER = LoggerFactory.getLogger(PlatformSetupApplication.class);

    private static final String ACTION_INIT = "init";
    private static final String ACTION_PUSH = "push";
    private static final String ACTION_PULL = "pull";

    @Autowired
    MemoryJNDISetup memoryJNDISetup;

    @Autowired
    PlatformSetup platformSetup;

    public static void main(String[] args) throws Exception {
        try {
            final String action = System.getProperty(PlatformSetup.BONITA_SETUP_ACTION);

            if (action != null) {
                switch (action) {
                    case ACTION_INIT:
                        init(getConfigurableApplicationContext(args));
                        break;
                    case ACTION_PUSH:
                        push(getConfigurableApplicationContext(args));
                        break;
                    case ACTION_PULL:
                        pull(getConfigurableApplicationContext(args));
                        break;
                    default:
                        displayMessageAndExit(action);
                }
            } else {
                displayMessageAndExit("null");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            // Exit code allows the calling script to catch an invalid execution:
            System.exit(1);
        }
    }

    private static ConfigurableApplicationContext getConfigurableApplicationContext(String[] args) throws PlatformException {
        new ConfigurationChecker().validate();
        return SpringApplication.run(PlatformSetupApplication.class, args);
    }

    private static void displayMessageAndExit(String action) {
        System.err.println("ERROR: unknown argument value for 'action': " + action);
        System.exit(1);
    }

    private static void pull(ConfigurableApplicationContext run) throws PlatformException {
        run.getBean(PlatformSetup.class).pull();
    }

    private static void push(ConfigurableApplicationContext run) throws PlatformException {
        run.getBean(PlatformSetup.class).push();
    }

    private static void init(ConfigurableApplicationContext run) throws PlatformException {
        run.getBean(PlatformSetup.class).init();
    }
}
