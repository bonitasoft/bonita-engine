/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.api;

import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;
import org.bonitasoft.engine.core.process.instance.model.SToken;

/**
 * Service that handle the creation/deletion of tokens.<br>
 * A token represent an active branch of a process.
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public interface TokenService {

    String PROCESS_INSTANCE_TOKEN_COUNT = "ACTIVITY_INSTANCE_TOKEN_COUNT";

    SToken createToken(Long processInstanceId, Long refId, Long parentRefId) throws SObjectCreationException;

    void createTokens(Long processInstanceId, Long refId, Long parentRefId, int numberOfToken) throws SObjectCreationException, SObjectModificationException,
            SObjectNotFoundException, SObjectReadException;

    void deleteTokens(Long processInstanceId, Long refId, int numberOfToken) throws SObjectModificationException, SObjectNotFoundException,
            SObjectReadException;

    void deleteToken(SToken token) throws SObjectModificationException;

    void deleteTokens(long processInstanceId) throws SObjectReadException, SObjectModificationException;

    int getNumberOfToken(long processInstanceId) throws SObjectReadException;

    int getNumberOfToken(long processInstanceId, long refId) throws SObjectReadException;

    SToken getToken(long processInstanceId, long refId) throws SObjectReadException, SObjectNotFoundException;

    void deleteAllTokens() throws SObjectReadException, SObjectModificationException;

}
