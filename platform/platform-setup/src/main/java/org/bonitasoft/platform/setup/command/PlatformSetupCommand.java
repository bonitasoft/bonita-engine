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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.setup.PlatformSetup;
import org.bonitasoft.platform.setup.PlatformSetupApplication;

/**
 * @author Baptiste Mesta
 */
public abstract class PlatformSetupCommand {

    private String name;
    private String summary;
    private String descriptionHeader;

    private String descriptionFooter;

    public PlatformSetupCommand(String name, String summary, String descriptionHeader, String descriptionFooter) {
        this.name = name;
        this.summary = summary;
        this.descriptionHeader = descriptionHeader;
        this.descriptionFooter = descriptionFooter;
    }

    public abstract void execute(Options options, CommandLine commandLine) throws PlatformException, CommandException;

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescriptionHeader() {
        return descriptionHeader;
    }

    public String getDescriptionFooter() {
        return descriptionFooter;
    }

    PlatformSetup getPlatformSetup(String[] args) throws PlatformException {
        return PlatformSetupApplication.getPlatformSetup(args);
    }
}
