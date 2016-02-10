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
package org.bonitasoft.engine.api.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.exception.StackTraceTransformer;

public class ServerSocketThread extends Thread {

    private final ServerSocket serverSocket;

    private final ServerAPI serverApi;

    public ServerSocketThread(final String name, final ServerAPI serverApi, final int port) throws IOException {
        super(name);
        this.serverApi = serverApi;
        serverSocket = new ServerSocket(port);

    }

    private Object invokeMethod(final MethodCall methodCall) throws ServerWrappedException, RemoteException {
        final Map<String, Serializable> options = methodCall.getOptions();
        final String apiInterfaceName = methodCall.getApiInterfaceName();
        final String methodName = methodCall.getMethodName();
        final List<String> classNameParameters = methodCall.getClassNameParameters();
        final Object[] parametersValues = methodCall.getParametersValues();
        try {
            return this.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
        } catch (final ServerWrappedException e) {
            throw StackTraceTransformer.mergeStackTraces(e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                final Socket clientSocket = serverSocket.accept();
                ObjectInputStream ois = null;
                ObjectOutputStream oos = null;
                try {
                    ois = new ObjectInputStream(clientSocket.getInputStream());
                    final MethodCall methodCall = (MethodCall) ois.readObject();
                    final Object callResult = invokeMethod(methodCall);
                    oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    oos.writeObject(callResult);
                    oos.flush();
                } catch (final Exception t) {
                    t.printStackTrace();
                } finally {
                    if (ois != null) {
                        ois.close();
                    }
                    if (oos != null) {
                        oos.close();
                    }
                    clientSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Object invokeMethod(final Map<String, Serializable> options, final String apiInterfaceName, final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws ServerWrappedException, RemoteException {
        return serverApi.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
    }
}
