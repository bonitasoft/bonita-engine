/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anthony Birembaut
 */
public class FormsResourcesUtils {

    /**
     * The forms directory name in the bar
     */
    public final static String FORMS_DIRECTORY_IN_BAR = "resources/forms";

    /**
     * Process UUID separator
     */
    public final static String UUID_SEPARATOR = "--";

    /**
     * Util class allowing to work with the BPM engine API
     */
    protected static final BPMEngineAPIUtil bpmEngineAPIUtil = new BPMEngineAPIUtil();

    /**
     * Logger
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(FormsResourcesUtils.class.getName());

    /**
     * Retrieve the web resources from the business archive and store them in a local directory
     *
     * @param session
     *        the engine API session
     * @param processDefinitionID
     *        the process definition ID
     * @param processDeploymentDate
     *        the process deployment date
     */
    public static synchronized void retrieveApplicationFiles(final APISession session, final long processDefinitionID,
            final Date processDeploymentDate)
            throws IOException, ProcessDefinitionNotFoundException, InvalidSessionException, RetrieveException,
            BPMEngineException {

        final ProcessAccessor process = new ProcessAccessor(bpmEngineAPIUtil.getProcessAPI(session));
        final File formsDir = getApplicationResourceDir(session, processDefinitionID, processDeploymentDate);
        if (!formsDir.exists()) {
            formsDir.mkdirs();
        }
        final Map<String, byte[]> formsResources = process.getResources(processDefinitionID,
                FORMS_DIRECTORY_IN_BAR + "/.*");
        for (final Entry<String, byte[]> formResource : formsResources.entrySet()) {
            final String filePath = formResource.getKey().substring(FORMS_DIRECTORY_IN_BAR.length() + 1);
            final byte[] fileContent = formResource.getValue();
            final File formResourceFile = new File(formsDir.getPath() + File.separator + filePath);
            final File formResourceFileDir = formResourceFile.getParentFile();
            if (!formResourceFileDir.exists()) {
                formResourceFileDir.mkdirs();
            }
            formResourceFile.createNewFile();
            if (fileContent != null) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(formResourceFile);
                    fos.write(fileContent);
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (final IOException e) {
                            if (LOGGER.isWarnEnabled()) {
                                LOGGER.warn("unable to close file output stream for business archive resource "
                                        + formResourceFile.getPath(), e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Delete the the web resources directory if it exists
     *
     * @param session
     *        the API session
     * @param processDefinitionID
     *        the process definition ID
     */
    public static synchronized void removeApplicationFiles(final APISession session, final long processDefinitionID) {

        try {
            final ProcessAPI processAPI = bpmEngineAPIUtil.getProcessAPI(session);
            final ProcessDefinition processDefinition = processAPI.getProcessDefinition(processDefinitionID);
            final String processUUID = processDefinition.getName() + UUID_SEPARATOR + processDefinition.getVersion();
            final File formsDir = new File(WebBonitaConstantsUtils.getTenantInstance().getFormsWorkFolder(),
                    processUUID);
            final boolean deleted = deleteDirectory(formsDir);
            if (!deleted) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("unable to delete the web resources directory " + formsDir.getCanonicalPath()
                            + ". You will be able to delete it manually once the JVM will shutdown");
                }
            }
        } catch (final Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while deleting the web resources directory for process " + processDefinitionID, e);
            }
        }
    }

    /**
     * Get the process resource directory
     */
    public static File getApplicationResourceDir(final APISession session, final long processDefinitionID,
            final Date processDeploymentDate)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, RetrieveException, BPMEngineException {
        final ProcessAccessor process = new ProcessAccessor(bpmEngineAPIUtil.getProcessAPI(session));
        final ProcessDefinition processDefinition = process.getDefinition(processDefinitionID);
        final String processUUID = processDefinition.getName() + UUID_SEPARATOR + processDefinition.getVersion();
        return new File(WebBonitaConstantsUtils.getTenantInstance().getFormsWorkFolder(), processUUID + File.separator
                + processDeploymentDate.getTime());
    }

    /**
     * Delete a directory and its content
     *
     * @param directory
     *        the directory to delete
     * @return return true if the directory and its content were deleted successfully, false otherwise
     */
    private static boolean deleteDirectory(final File directory) {
        boolean success = true;;
        if (directory.exists()) {
            final File[] files = directory.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    success &= deleteDirectory(files[i]);
                } else {
                    success &= files[i].delete();
                }
            }
            success &= directory.delete();
        }
        return success;
    }
}
