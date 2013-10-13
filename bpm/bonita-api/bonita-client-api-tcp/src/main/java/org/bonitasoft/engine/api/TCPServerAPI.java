/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.api.tcp.MethodCall;
import org.bonitasoft.engine.exception.ServerAPIException;

/**
 * @author Matthieu Chaffotte
 */
public class TCPServerAPI implements ServerAPI {

    private static final long serialVersionUID = 1L;
    private Map<String, String> parameters;

    public TCPServerAPI(final Map<String, String> parameters) throws ServerAPIException {
        this.parameters = parameters;
        System.err.println(this.getClass().getSimpleName() + " - constructor...");
    }

    @Override
    public Object invokeMethod(final Map<String, Serializable> options, final String apiInterfaceName, final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws ServerWrappedException, RemoteException {

        final String host = parameters.get("host");
        final int port = Integer.parseInt(parameters.get("port"));

        System.err.println(this.getClass().getSimpleName() + " - invoking: with parameters: " 
                + ", options: " + options
                + ", apiInterfaceName: " + apiInterfaceName
                + ", methodName: " + methodName
                + ", classNameParameters: " + classNameParameters
                + ", parametersValues: " + parametersValues
                + "...");
        Socket remoteServerAPI = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            System.err.println(this.getClass().getSimpleName() + " - building a clientSocket...");
            remoteServerAPI = new Socket(host, port);
            System.err.println(this.getClass().getSimpleName() + " - client socket buit: " + remoteServerAPI);
            final InputStream socketInputStream = remoteServerAPI.getInputStream();
            oos = new ObjectOutputStream(remoteServerAPI.getOutputStream());
            final MethodCall methodCall = new MethodCall(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
            System.err.println(this.getClass().getSimpleName() + " - invoking with methodCall: " + methodCall);
            oos.writeObject(methodCall);
            oos.flush();
            System.err.println(this.getClass().getSimpleName() + " - flushed, waiting for retun...");
            ois = new ObjectInputStream(socketInputStream);
            final Object callReturn = ois.readObject();
            System.err.println(this.getClass().getSimpleName() + " - received return: " + callReturn);
            return checkInvokeMethodReturn(callReturn);
        } catch (Throwable e) {
            throw new ServerWrappedException(e);
        } finally {

            try {
                if (oos != null) {
                    oos.close();
                }
                if (ois != null) {
                    ois.close();
                }
                if (remoteServerAPI != null) {
                    remoteServerAPI.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private Object checkInvokeMethodReturn(final Object callReturn) throws Throwable {
        System.err.println(this.getClass().getSimpleName() + " - checking calReturn...");
        if (callReturn != null && callReturn instanceof Throwable) {
            final Throwable throwable = (Throwable) callReturn; 
            System.err.println(this.getClass().getSimpleName() + " - callReturn was an exception, throwing it: " + throwable.getClass() + ": " + throwable.getMessage());
            throw throwable;
        }
        System.err.println(this.getClass().getSimpleName() + " - returning calReturn as it was received...");
        return callReturn;
    }

}
