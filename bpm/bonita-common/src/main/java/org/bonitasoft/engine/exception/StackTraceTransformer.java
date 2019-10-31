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
package org.bonitasoft.engine.exception;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.internal.ServerWrappedException;

/**
 * Transform an exception having causes in one single exception with merged stack trace
 * This is done in order to avoid to throw to client server side exception with unknown class in client side
 *
 * @author Baptiste Mesta
 */
public class StackTraceTransformer {

    private final ServerWrappedException e;

    static Field field;

    static {
        try {
            field = Throwable.class.getDeclaredField("cause");
            field.setAccessible(true);
        } catch (final NoSuchFieldException e) {
            e.printStackTrace();
        } catch (final SecurityException e) {
            e.printStackTrace();
        }
    }

    public StackTraceTransformer(final ServerWrappedException e) {
        this.e = e;
    }

    /**
     * @param e
     *        the ServerWrappedException given by the server api
     * @return
     *         a safe to throw to client ServerWrappedException
     */
    public static ServerWrappedException mergeStackTraces(final ServerWrappedException e) {
        try {
            return new StackTraceTransformer(e).merge();
        } catch (final Exception e1) {
            System.err.println("Unable to throw the root exception: " + e1.getClass().getName() + ": " + e1.getMessage());
            e1.printStackTrace();
            return new ServerWrappedException(new BonitaRuntimeException(
                    "Unable to throw the root exception because of (see log for the original stack trace)", e));
        }
    }

    public static void addStackTo(final Throwable e, final StackTraceElement[] clientStackTrace) {
        final StackTraceElement[] causeStack = e.getStackTrace();

        final StackTraceElement[] newStack = new StackTraceElement[causeStack.length + clientStackTrace.length + 1];

        System.arraycopy(clientStackTrace, 0, newStack, 0, clientStackTrace.length);
        newStack[clientStackTrace.length] = new StackTraceElement("\t< ========== Beginning of the server stack trace ========== >", " ", " ", -3);
        System.arraycopy(causeStack, 0, newStack, clientStackTrace.length + 1, causeStack.length);
        e.setStackTrace(newStack);
    }

    private ServerWrappedException merge() throws Exception {
        final Throwable cause = e.getCause();
        if (field != null) {
            transfertStack(cause, cause);
            field.set(cause, null);
            return e;
        }
        Throwable newCause;
        if (cause.getMessage() != null) {
            newCause = cause.getClass().getConstructor(String.class).newInstance(e.getMessage());
        } else {
            newCause = cause.getClass().newInstance();
        }
        transfertStack(newCause, cause);
        return new ServerWrappedException(newCause);
    }

    private void transfertStack(final Throwable mergeStackInside, final Throwable cause) {
        Throwable subCause = cause.getCause();
        if (subCause == null) {
            // no stak to merge
            return;
        }
        final StackTraceElement[] currentStack = cause.getStackTrace();
        final List<StackTraceElement[]> causesStacks = new ArrayList<StackTraceElement[]>();
        final List<Integer> framesInCommons = new ArrayList<Integer>();
        final List<Throwable> exceptions = new ArrayList<Throwable>();
        int causeslength = 0;
        StackTraceElement[] lastStack = currentStack;
        do {
            final StackTraceElement[] trace = subCause.getStackTrace();
            causesStacks.add(trace);
            exceptions.add(subCause);

            int m = trace.length - 1;
            int n = lastStack.length - 1;
            while (m >= 0 && n >= 0 && trace[m].equals(lastStack[n])) {
                m--;
                n--;
            }
            final int framesInCommon = trace.length - 1 - m;
            framesInCommons.add(framesInCommon);
            lastStack = trace;
            // add remove the frames in common to the total length but add one to put the "...23 more" if there is some in common
            causeslength += trace.length + 1 - framesInCommon + (framesInCommon == 0 ? 0 : 1);
        } while ((subCause = subCause.getCause()) != null);
        final StackTraceElement[] mergedStackTrace = new StackTraceElement[currentStack.length + causeslength];
        System.arraycopy(currentStack, 0, mergedStackTrace, 0, currentStack.length);
        int current = currentStack.length;
        int i = 0;
        for (final StackTraceElement[] stackTraceElements : causesStacks) {
            final Integer framesInCommon = framesInCommons.get(i);
            mergedStackTrace[current] = new StackTraceElement("\tCaused by: " + exceptions.get(i).getClass().getName(), ": "
                    + exceptions.get(i).getMessage() + " ", " ",
                    -3);
            current++;
            System.arraycopy(stackTraceElements, 0, mergedStackTrace, current, stackTraceElements.length - framesInCommon);
            current += stackTraceElements.length - framesInCommon;
            if (framesInCommon != 0) {
                mergedStackTrace[current] = new StackTraceElement("... " + framesInCommon + " more", " ", " ", -3);
                current++;
            }
            i++;
        }
        mergeStackInside.setStackTrace(mergedStackTrace);
    }

    /*
     * Reduce stack length
     */

}
