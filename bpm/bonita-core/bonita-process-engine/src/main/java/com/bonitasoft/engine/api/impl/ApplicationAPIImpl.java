/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;

import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationNotFoundException;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationAPIImpl implements ApplicationAPI {

    @Override
    public Application createApplication(ApplicationCreator applicationCreator) throws AlreadyExistsException, CreationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Application getApplication(long applicationId) throws ApplicationNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteApplication(long applicationId) throws DeletionException {
        // TODO Auto-generated method stub

    }

}
