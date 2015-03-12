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
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.impl.ServerAPIFactory;
import org.bonitasoft.engine.api.internal.ServerAPI;

/**
 * @author Charles Souillard
 */
public class TcpServer {

    private final List<ServerSocketThread> sockets = new ArrayList<ServerSocketThread>();

    public TcpServer(final List<Integer> ports) throws InterruptedException, IOException {
        final int nbOfPorts = ports.size();
        int i = 1;
        final ServerAPI apiImpl = ServerAPIFactory.getServerAPI();
        for (final int port : ports) {
            final String threadName = ServerSocketThread.class.getSimpleName() + "-" + String.format("%" + nbOfPorts + "d", i) + "-p" + port;
            sockets.add(new ServerSocketThread(threadName, apiImpl, port));
            i++;
        }
        for (final ServerSocketThread socket : sockets) {
            socket.start();
        }
        for (final ServerSocketThread socket : sockets) {
            socket.join();
        }
    }

}
