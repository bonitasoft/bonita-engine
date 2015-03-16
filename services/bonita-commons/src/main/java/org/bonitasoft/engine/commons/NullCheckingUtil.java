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
package org.bonitasoft.engine.commons;

import java.util.BitSet;

/**
 * @author Elias Ricken de Medeiros
 */
public class NullCheckingUtil {

    /**
     * Check that the given parameters are not null.
     * This method should only be used to check that some parameters given to a
     * given method are not null. The exception message tries its best to produce
     * a helpful message by scanning the stack trace.
     * 
     * @param params
     *            the parameters to check
     * @throws IllegalArgumentException
     *             if at least one of the parameters is null
     */
    public static void checkArgsNotNull(final Object... params) {
        checkArgsNotNull(1, params);
    }

    /**
     * Check that the given parameters are not null.
     * This method should only be used to check that some parameters given to a
     * given method are not null. The exception message tries its best to produce
     * a helpful message by scanning the stack trace.
     * 
     * @param offset
     *            the offset to use in the stack trace to produce error message
     * @param params
     *            the parameters to check
     * @throws IllegalArgumentException
     *             if at least one of the parameters is null
     */
    private static void checkArgsNotNull(final int offset, final Object... params) {
        final NullCheckResult result = findNull(params);
        if (result.hasNull()) {
            // Guess the signature of the caller
            final StackTraceElement callerSTE = getCaller(offset + 1);
            final String className = callerSTE.getClassName();
            final String methodName = callerSTE.getMethodName();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < result.getSize(); i++) {
                if (result.isNull(i)) {
                    sb.append("null");
                } else {
                    sb.append(params[i].getClass().getName());
                }
                if (i < result.getSize() - 1) {
                    sb.append(", ");
                }
            }

            final StringBuilder messageStb = new StringBuilder("Some parameters are null in ");
            messageStb.append(className);
            messageStb.append('.');
            messageStb.append(methodName);
            messageStb.append("(): ");
            messageStb.append(sb.toString());
            throw new IllegalArgumentException(messageStb.toString());
        }
    }

    /**
     * Find null parameters in the given list.
     * This method returns a {@link NullCheckResult}.
     * 
     * @param params
     *            the parameters to check
     * @return a {@link NullCheckResult} representing null parameters.
     * @see NullCheckResult
     */
    public static NullCheckResult findNull(final Object... params) {
        if (params == null) {
            final BitSet bitSet = new BitSet(1);
            bitSet.set(0);
            return new NullCheckResult(bitSet, 1);
        }
        final BitSet bitSet = new BitSet(params.length);
        for (int i = 0; i < params.length; i++) {
            bitSet.set(i, params[i] == null);
        }
        return new NullCheckResult(bitSet, params.length);
    }

    /**
     * Return the StackTraceElement at the given offset from this method
     * invocation.
     * 
     * @param offset
     * @return a StackTraceElement
     */
    public static StackTraceElement getCaller(final int offset) {
        final StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        StackTraceElement callerSTE = null;
        for (int i = 0; i < stes.length - offset - 1; i++) {
            if (stes[i].getClassName().equals(NullCheckingUtil.class.getName()) && stes[i].getMethodName().equals("getCaller")) {
                callerSTE = stes[i + 1 + offset];
                break;
            }
        }
        badStateIfNull(callerSTE, "Ouch! Can't get the stack trace back to the caller of this method!");
        return callerSTE;
    }

    /**
     * This method throw an IllegalStateException if the given parameter is null
     * 
     * @param valueToCheck
     *            the value to check
     * @param msg
     *            the message for the thrown exception
     * @see IllegalStateException
     */
    public static void badStateIfNull(final Object valueToCheck, final String msg) {
        badStateIfTrue(valueToCheck == null, msg);
    }

    /**
     * This method throw an IllegalStateException if the given parameter is true
     * 
     * @param valueToCheck
     *            the value to check
     * @param msg
     *            the message for the thrown exception
     * @see IllegalStateException
     */
    public static void badStateIfTrue(final boolean valueToCheck, final String msg) {
        if (valueToCheck) {
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Represents null value returned by {@link #findNull(Object...)}.
     * 
     * @see #findNull(Object...)
     */
    public static class NullCheckResult {

        private final int size;

        private final BitSet bitSet;

        NullCheckResult(final BitSet bitSet, final int size) {
            this.bitSet = bitSet;
            this.size = size;
        }

        /**
         * Returns true if some parameters given to {@link #findNull(Object...)} were null.
         * 
         * @return true if some parameters given to {@link #findNull(Object...)} were null.
         * @see #findNull(Object...)
         */
        public boolean hasNull() {
            return bitSet.cardinality() != 0;
        }

        /**
         * Returns the number of parameters given to {@link #findNull(Object...)}
         * 
         * @return the number of parameters given to {@link #findNull(Object...)}
         * @see #findNull(Object...)
         */
        public int getSize() {
            return size;
        }

        /**
         * Returns true if the i th parameter given to {@link #findNull(Object...)} was null.
         * 
         * @param i
         *            the rank of the parameter given to {@link #findNull(Object...)}.
         * @return true if the i th parameter given to {@link #findNull(Object...)} was null.
         */
        public boolean isNull(final int i) {
            return bitSet.get(i);
        }
    }
}
