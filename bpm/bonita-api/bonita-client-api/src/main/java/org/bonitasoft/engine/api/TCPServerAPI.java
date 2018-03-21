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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.api.tcp.MethodCall;

/**
 * @author Matthieu Chaffotte
 */
public class TCPServerAPI implements ServerAPI {

    private static final long serialVersionUID = 1L;

    private final List<TcpDestination> destinations = new ArrayList<TcpDestination>();

    private final Random random;

    public TCPServerAPI(final Map<String, String> parameters) {
        final String destinationsList = parameters.get("destinations");
        final String[] splittedDestinations = destinationsList.split(",");
        for (final String destination : splittedDestinations) {
            destinations.add(getTcpdDestinationFromPattern(destination));
        }
        random = new Random();
    }

    private TcpDestination getTcpdDestinationFromPattern(final String s) {
        final int separatorIndex = s.indexOf(':');
        final String host = s.substring(0, separatorIndex);
        final int port = Integer.valueOf(s.substring(separatorIndex + 1));
        return new TcpDestination(host, port);
    }

    @Override
    public Object invokeMethod(final Map<String, Serializable> options, final String apiInterfaceName, final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws ServerWrappedException {
        try {
            Socket remoteServerAPI = null;
            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;
            try {
                final TcpDestination tcpDestination = destinations.get(random.nextInt(destinations.size()));
                remoteServerAPI = new Socket(tcpDestination.getHost(), tcpDestination.getPort());
                final InputStream socketInputStream = remoteServerAPI.getInputStream();
                oos = new ObjectOutputStream(remoteServerAPI.getOutputStream());
                final MethodCall methodCall = new MethodCall(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
                oos.writeObject(methodCall);
                oos.flush();
                ois = new ObjectInputStream(socketInputStream);
                final Object callReturn = ois.readObject();
                return checkInvokeMethodReturn(callReturn);

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
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (final Exception e) {
            throw new ServerWrappedException(e);
        }
    }

    private Object checkInvokeMethodReturn(final Object callReturn) throws Exception {
        if (callReturn instanceof Throwable) {
            final Exception throwable = (Exception) callReturn;
            throw throwable;
        }
        return callReturn;
    }

}
