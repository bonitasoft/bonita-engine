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
package org.bonitasoft.engine.api;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.exception.ServerAPIException;

/**
 * @author Matthieu Chaffotte
 * @author Aurelien Pupier
 * @author Julien Reboul
 */

public class EJB3ServerAPI implements ServerAPI {

    public static final String EJB_NAMING_REFERENCE_PROPERTY = "org.bonitasoft.engine.ejb.naming.reference";

    private static final long serialVersionUID = 1L;

    protected volatile ServerAPI remoteServAPI;

    protected static final String SERVER_API_BEAN_NAME_JBOSS7 = "ejb:bonita-ear/bonita-ejb/serverAPIBean!org.bonitasoft.engine.api.internal.ServerAPI";

    protected Map<String, String> parameters;

    public EJB3ServerAPI(final Map<String, String> parameters) throws ServerAPIException {
        this.parameters = parameters;
        initServerAPIReference();
    }

    public EJB3ServerAPI() throws RemoteException {
    }

    protected ServerAPI lookup(final String name, final Hashtable<String, String> environment) throws NamingException {
        InitialContext initialContext = null;
        if (environment != null) {
            initialContext = new InitialContext(environment);
        } else {
            initialContext = new InitialContext();
        }
        return (ServerAPI) initialContext.lookup(name);
    }

    @Override
    public Object invokeMethod(final Map<String, Serializable> options, final String apiInterfaceName, final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws ServerWrappedException, RemoteException {
        return remoteServAPI.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
    }

    protected void initServerAPIReference() throws ServerAPIException {
        String serverAPIBeanName;
        if (parameters == null || (serverAPIBeanName = parameters.get(EJB_NAMING_REFERENCE_PROPERTY)) == null) {
            serverAPIBeanName = SERVER_API_BEAN_NAME_JBOSS7;
        }
        try {
            remoteServAPI = lookup(serverAPIBeanName, new Hashtable<String, String>(
                    parameters));
        } catch (final NamingException e) {
            throw new ServerAPIException("[" + serverAPIBeanName + "] Reference To Server API does not exists. Edit bonita-client.properties#"
                    + EJB3ServerAPI.EJB_NAMING_REFERENCE_PROPERTY + " property to change the reference name", e);
        }
    }
}
