/**
 * Copyright (C) 2019 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.application;

import static org.bonitasoft.engine.api.impl.application.deployer.validator.ArtifactValidatorFactory.artifactValidator;

import java.io.IOException;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.impl.application.deployer.ApplicationArchiveReader;
import org.bonitasoft.engine.api.impl.application.deployer.Deployer;
import org.bonitasoft.engine.api.result.ExecutionResult;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.exception.APIImplementationNotFoundException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.DeployerException;
import org.bonitasoft.engine.service.APIAccessResolver;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;

/**
 * @author Emmanuel Duchastenier
 */
public class ApplicationAPIDelegate {

    public ExecutionResult deployApplication(byte[] applicationArchive) throws DeployerException {
        final APIAccessResolver apiAccessResolver = getApiAccessResolver();
        final Deployer deployer = Deployer.builder()
                .pageAPI(getApi(apiAccessResolver, PageAPI.class))
                .applicationAPI(getApi(apiAccessResolver, ApplicationAPI.class))
                .processAPI(getApi(apiAccessResolver, ProcessAPI.class))
                .applicationArchiveReader(new ApplicationArchiveReader(artifactValidator()))
                .artifactValidator(artifactValidator())
                .build();
        deployer.deploy(applicationArchive);
        return ExecutionResult.OK;
    }

    @SuppressWarnings("unchecked")
    private <T> T getApi(APIAccessResolver apiAccessResolver, Class<T> apiClass) {
        try {
            return (T) apiAccessResolver.getAPIImplementation(apiClass.getName());
        } catch (APIImplementationNotFoundException e) {
            throw new SBonitaRuntimeException(e);
        }
    }

    private APIAccessResolver getApiAccessResolver() {
        try {
            return ServiceAccessorFactory.getInstance().createAPIAccessResolver();
        } catch (BonitaHomeNotSetException | IOException | BonitaHomeConfigurationException
                | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new SBonitaRuntimeException(e);
        }
    }

}
