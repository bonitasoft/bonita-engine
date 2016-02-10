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
package org.bonitasoft.engine.test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.connector.AbstractConnector;

public class BlockingConnector extends AbstractConnector {

    public static Semaphore semaphore = new Semaphore(1);

    @Override
    public void validateInputParameters() {
    }

    @Override
    protected void executeBusinessLogic() {
        try {
            System.out.println("Try aqcuire in connector");
            semaphore.tryAcquire(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        semaphore.release();
        System.out.println("semaphore in connector released");
    }
}
