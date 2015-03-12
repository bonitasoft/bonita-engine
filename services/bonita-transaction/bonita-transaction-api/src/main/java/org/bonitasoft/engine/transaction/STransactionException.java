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
package org.bonitasoft.engine.transaction;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

public class STransactionException extends SBonitaException {

    private static final long serialVersionUID = 8650941405945187337L;

    private final List<Throwable> exceptions;

    public STransactionException(final String message, final List<Throwable> commitExceptions) {
        super(message);
        exceptions = Collections.unmodifiableList(commitExceptions);
    }

    public STransactionException(final String message, final Throwable throwable) {
        super(message, throwable);
        exceptions = Collections.<Throwable>emptyList();
    }

    public STransactionException(final Throwable throwable) {
        super(throwable);
        exceptions = Collections.<Throwable>emptyList();
    }

    public STransactionException(final String message) {
        super(message);
        exceptions = Collections.<Throwable>emptyList();
    }

    public List<Throwable> getCommitExceptions() {
        return exceptions;
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
        for (final Throwable throwable : exceptions) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void printStackTrace(final PrintStream s) {
        super.printStackTrace(s);
        for (final Throwable throwable : exceptions) {
            throwable.printStackTrace(s);
        }
    }

    @Override
    public void printStackTrace(final PrintWriter s) {
        super.printStackTrace(s);
        for (final Throwable throwable : exceptions) {
            throwable.printStackTrace(s);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Throwable#getCause()
     */
    @Override
    public Throwable getCause() {
        Throwable cause = super.getCause();
        if (cause == null && !exceptions.isEmpty()) {
            cause = exceptions.get(0);
        }
        return cause;
    }

}
