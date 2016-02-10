/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.bar;

import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.io.IOUtil;

/**
 * @author Emmanuel Duchastenier
 */
public class ProcessDefinitionBARContributionExt extends ProcessDefinitionBARContribution {

    @Override
    protected String getProcessInfos(final String infos) {
        return Base64.encodeBase64String(DigestUtils.sha1(infos)).trim();
    }

    @Override
    protected void checkProcessInfos(final File barFolder, final DesignProcessDefinition processDefinition) throws InvalidBusinessArchiveFormatException {
        try {
            super.checkProcessInfos(barFolder, processDefinition);
        } catch (final InvalidBusinessArchiveFormatException inv) {
            final String processInfos = super.getProcessInfos(generateInfosFromDefinition(processDefinition));
            String fileContent;
            try {
                fileContent = IOUtil.read(new File(barFolder, PROCESS_INFOS_FILE)).trim();
                if (!processInfos.equals(fileContent)) {
                    throw new InvalidBusinessArchiveFormatException("Invalid Business Archive format");
                }
            } catch (final IOException e) {
                throw new InvalidBusinessArchiveFormatException("Invalid Business Archive format");
            }
        }
    }

}
