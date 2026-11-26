/**
 * Copyright (C) 2024 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.mdc;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.slf4j.MDC;

/**
 * <p>Provides usefull methods for working with MDC.</p>
 * <p>Use the various {@link #tryWithMDC} methods to execute a runnable or a callable with MDC information in the
 * context.<br/>
 * You may provide a context {@link Supplier} directly, or a key object for which a {@link Supplier} was previously
 * registered with {@link #supplyMDC}
 * methods.</p>
 * <p>Use the appropriate Runnable or Callable implementation dependending on the number of Exception you want to
 * throw.<br/>
 * When used with lambdas, lambda will take only the common exception superclass. So instead of writing
 *
 * <pre>
 * Supplier&lt;AbstractMDC&gt; mdc = () -&gt; new AbstractMDC(Map.of(key, value)) {};
 * MDCHelper.tryWithMDC(mdc, ()->{
 *     // throws Exception1 or Exception2
 *     ...
 * });
 * </pre>
 *
 * You should declare the type and write
 *
 * <pre>
 * Supplier&lt;AbstractMDC&gt; mdc = () -&gt; new AbstractMDC(Map.of(key, value)) {};
 * CheckedRunnable2&lt;Exception1, Exception2&gt; run = ()->{
 *     // throws Exception1 or Exception2
 *     ...
 * };
 * MDCHelper.tryWithMDC(mdc, run);
 * </pre>
 *
 * <p>When an exception is thrown within a {@link #tryWithMDC} method and not caught by the Runnable or Callable, the
 * current context is registered with
 * {@link #supplyMDC} for the exception.<br/>
 * So calling {@link #tryWithMDC(Object, CheckedRunnable4)} with the exception as <code>usingObject</code> key will
 * allow you to log the exception with the
 * original context.<br/>
 * You can even call this method when no context was supplied, as it would have no noticeable effect.</p>
 *
 * @see AbstractMDC the base implementation for all MDC classes
 */
public class MDCHelper {

    /**
     * Like {@link Runnable} but throwing 4 checked exceptions
     */
    @FunctionalInterface
    public interface CheckedRunnable4<E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable> {

        void run() throws E1, E2, E3, E4;
    }

    /**
     * Like {@link Runnable} but throwing 3 checked exceptions
     */
    @FunctionalInterface
    public interface CheckedRunnable3<E1 extends Throwable, E2 extends Throwable, E3 extends Throwable>
            extends CheckedRunnable4<E1, E2, E3, E1> {
    };

    /**
     * Like {@link Runnable} but throwing 2 checked exceptions
     */
    @FunctionalInterface
    public interface CheckedRunnable2<E1 extends Throwable, E2 extends Throwable>
            extends CheckedRunnable3<E1, E2, E1> {
    };

    /**
     * Like {@link Runnable} but throwing a checked exception
     */
    @FunctionalInterface
    public interface CheckedRunnable<E extends Throwable>
            extends CheckedRunnable2<E, E> {
    };

    /**
     * Like {@link Callable} but throwing 4 checked exceptions
     */
    @FunctionalInterface
    public interface CheckedCallable4<V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable> {

        V call() throws E1, E2, E3, E4;
    }

    /**
     * Like {@link Callable} but throwing 3 checked exceptions
     */
    @FunctionalInterface
    public interface CheckedCallable3<V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable>
            extends CheckedCallable4<V, E1, E2, E3, E1> {
    }

    /**
     * Like {@link Callable} but throwing 2 checked exceptions
     */
    @FunctionalInterface
    public interface CheckedCallable2<V, E1 extends Throwable, E2 extends Throwable>
            extends CheckedCallable3<V, E1, E2, E1> {
    }

    /**
     * Like {@link Callable} but throwing a checked exception
     */
    @FunctionalInterface
    public interface CheckedCallable<V, E extends Throwable> extends CheckedCallable2<V, E, E> {
    }

    private static Map<Object, MDCHelper> instances = new WeakHashMap<>();

    /** Builds the MDC */
    private Supplier<? extends AbstractMDC> supplier;

    private MDCHelper(Supplier<? extends AbstractMDC> mdcSupplier) {
        supplier = mdcSupplier;
    }

    /**
     * Make a supplier that will get the current context, to create a similar MDC in its time (usually in another
     * thread)
     *
     * @return MDC supplier holding information from current context in this thread
     */
    public static Supplier<? extends AbstractMDC> makeCurrentContextSupplier() {
        var contextMap = MDC.getCopyOfContextMap();
        return () -> new AbstractMDC(contextMap) {
        };
    }

    /**
     * Supply a MDC to an object that may use it later.
     *
     * @param mdcSupplier supplies a MDC
     * @param usingObject the object that will need MDC
     */
    public static void supplyMDC(Supplier<? extends AbstractMDC> mdcSupplier, Object usingObject) {
        supplyMDC(mdcSupplier, usingObject, true);
    }

    /**
     * Supply a MDC to an object that may use it later.
     *
     * @param mdcSupplier supplies a MDC
     * @param usingObject the object that will need MDC
     * @param overwrite whether to overwrite an existing supplier
     */
    public static void supplyMDC(Supplier<? extends AbstractMDC> mdcSupplier, Object usingObject, boolean overwrite) {
        if (overwrite) {
            instances.put(usingObject, new MDCHelper(mdcSupplier));
        } else {
            // do not build a new Helper when one is already present
            instances.computeIfAbsent(usingObject, k -> new MDCHelper(mdcSupplier));
        }
    }

    /**
     * Get an MDC, to encapsulate in a try with.
     *
     * @param usingObject the object using the MDC, for which a supplier may have been provided
     * @return the supplied MDC or null
     */
    public static AbstractMDC getMDC(Object usingObject) {
        var helper = getHelper(usingObject);
        return helper.map(h -> h.supplier).map(Supplier::get).orElse(null);
    }

    /**
     * Get helper associated to using object.
     *
     * @param usingObject the object using the MDC, for which a supplier may have been provided
     * @return the associated helper
     */
    private static Optional<MDCHelper> getHelper(Object usingObject) {
        var helper = Optional.ofNullable(instances.remove(usingObject));
        // when not present, try and take the context associated to the cause exception if available
        if (!helper.isPresent() && usingObject instanceof Throwable) {
            var cause = ((Throwable) usingObject).getCause();
            return getHelper(cause);
        }
        return helper;
    }

    /**
     * Try and invoke a callable with MDC
     *
     * @param <V> the result type
     * @param mdcSupplier supplies a MDC
     * @param callable the callable with result
     * @return the callable result
     * @throws E exception in callable
     */
    public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable> V tryWithMDC(
            Supplier<? extends AbstractMDC> mdcSupplier,
            CheckedCallable4<V, E1, E2, E3, E4> callable) throws E1, E2, E3, E4 {
        try (var mdc = mdcSupplier.get()) {
            try {
                return callable.call();
            } catch (Throwable exception) {
                // attach context to exception before rethrowing it
                supplyMDC(makeCurrentContextSupplier(), exception, false);
                throw exception;
            }
        }
    }

    /**
     * Try and invoke a callable with MDC
     *
     * @param <V> the result type
     * @param usingObject the object using the MDC, for which a supplier may have been provided
     * @param callable the callable with result
     * @return the callable result
     * @throws E exception in callable
     */
    public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable> V tryWithMDC(
            Object usingObject, CheckedCallable4<V, E1, E2, E3, E4> callable) throws E1, E2, E3, E4 {
        try (var mdc = getMDC(usingObject)) {
            try {
                return callable.call();
            } catch (Throwable exception) {
                // attach context to exception before rethrowing it
                supplyMDC(makeCurrentContextSupplier(), exception, false);
                throw exception;
            }
        }
    }

    /**
     * Try and invoke a runnable with MDC
     *
     * @param mdcSupplier supplies a MDC
     * @param runnable the runnable (without result)
     * @throws E exception in runnable
     */
    public static <E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable> void tryWithMDC(
            Supplier<? extends AbstractMDC> mdcSupplier,
            CheckedRunnable4<E1, E2, E3, E4> runnable)
            throws E1, E2, E3, E4 {
        CheckedCallable4<Void, E1, E2, E3, E4> callable = () -> {
            runnable.run();
            return null;
        };
        tryWithMDC(mdcSupplier, callable);
    }

    /**
     * Try and invoke a runnable with MDC
     *
     * @param usingObject the object using the MDC, for which a supplier may have been provided
     * @param runnable the runnable (without result)
     * @throws E exception in runnable
     */
    public static <E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable> void tryWithMDC(
            Object usingObject, CheckedRunnable4<E1, E2, E3, E4> runnable) throws E1, E2, E3, E4 {
        CheckedCallable4<Void, E1, E2, E3, E4> callable = () -> {
            runnable.run();
            return null;
        };
        tryWithMDC(usingObject, callable);
    }

}
