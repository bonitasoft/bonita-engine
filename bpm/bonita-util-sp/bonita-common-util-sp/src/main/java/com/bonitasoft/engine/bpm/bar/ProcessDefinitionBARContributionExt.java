/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package com.bonitasoft.engine.bpm.bar;

import java.io.File;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.exception.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.util.IOUtil;

/**
 * @author Emmanuel Duchastenier
 */
public class ProcessDefinitionBARContributionExt extends org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution {

    @Override
    protected String getProcessInfos(final String infos) {
        return Base64.encodeBase64String(DigestUtils.sha1(infos)).trim();
    }

    @Override
    protected void checkProcessInfos(final File barFolder, final DesignProcessDefinition processDefinition) throws InvalidBusinessArchiveFormatException {
        try {
            super.checkProcessInfos(barFolder, processDefinition);
        } catch (InvalidBusinessArchiveFormatException inv) {
            final String processInfos = super.getProcessInfos(generateInfosFromDefinition(processDefinition));
            final String fileContent = IOUtil.getFileContent(new File(barFolder, PROCESS_INFOS_FILE)).trim();
            if (!processInfos.equals(fileContent)) {
                throw new InvalidBusinessArchiveFormatException("Invalid Business Archive format");
            }
        }
    }
}
