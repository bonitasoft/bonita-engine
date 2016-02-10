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

import java.util.Map;

import org.bonitasoft.engine.work.BonitaWork;

final class FailingWork extends BonitaWork {

    private static final long serialVersionUID = 1L;

    @Override
    public String getDescription() {
        return "MyJob";
    }

    @Override
    public String getRecoveryProcedure() {
        return "The recovery procedure";
    }

    @Override
    public void work(final Map<String, Object> context) throws Exception {
        throw new Exception("an unexpected exception");

    }

    @Override
    public void handleFailure(final Exception e, final Map<String, Object> context) throws Exception {
        throw new Exception("unable to handle failure");
    }
}
