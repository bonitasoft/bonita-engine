/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.exception.ServerAPIException;

/**
 * @author Matthieu Chaffotte
 */
public class EJB2ServerAPI implements ServerAPI {

    private static final long serialVersionUID = 1L;

    private final ServerAPI remoteServAPI;

    public EJB2ServerAPI(final Map<String, String> parameters) throws ServerAPIException {
        try {
            final Object serverAPI = lookup("serverAPI", new Hashtable<String, String>(parameters));
            final ServerAPIHome accessorHome = (ServerAPIHome) PortableRemoteObject.narrow(serverAPI, ServerAPIHome.class);
            remoteServAPI = accessorHome.create();
        } catch (final NamingException ne) {
            throw new ServerAPIException(ne);
        } catch (final RemoteException re) {
            throw new ServerAPIException(re);
        } catch (final CreateException ce) {
            throw new ServerAPIException(ce);
        }
    }

    private Object lookup(final String name, final Hashtable<String, String> environment) throws NamingException {
        InitialContext initialContext = null;
        if (environment != null) {
            initialContext = new InitialContext(environment);
        } else {
            initialContext = new InitialContext();
        }
        return initialContext.lookup(name);
    }

    @Override
    public Object invokeMethod(final Map<String, Serializable> options, final String apiInterfaceName, final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws ServerWrappedException, RemoteException {
        return remoteServAPI.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
    }

}
