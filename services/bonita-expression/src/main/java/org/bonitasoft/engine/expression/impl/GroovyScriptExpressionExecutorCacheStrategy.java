/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.expression.impl;

import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.classloader.SingleClassLoaderListener;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.NonEmptyContentExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.typehandling.GroovyCastException;

public class GroovyScriptExpressionExecutorCacheStrategy extends NonEmptyContentExpressionExecutorStrategy
        implements SingleClassLoaderListener {

    public static final String GROOVY_SCRIPT_CACHE_NAME = "GROOVY_SCRIPT_CACHE_NAME";

    public static final String SCRIPT_KEY = "SCRIPT_";
    public static final String COERCION_SCRIPT_KEY = "COERCION_SCRIPT_";
    public static final String SHELL_KEY = "SHELL_";

    private final CacheService cacheService;

    private final ClassLoaderService classLoaderService;

    private final TechnicalLoggerService logger;

    private static final AtomicLong counter = new AtomicLong();

    public GroovyScriptExpressionExecutorCacheStrategy(final CacheService cacheService,
            final ClassLoaderService classLoaderService,
            final TechnicalLoggerService logger) {
        this.cacheService = cacheService;
        this.classLoaderService = classLoaderService;
        this.logger = logger;
    }

    private String generateScriptName() {
        return String.format("BScript%s.groovy", counter.incrementAndGet());
    }

    Class getScriptFromCache(final String expressionContent, final Long definitionId)
            throws SCacheException, SClassLoaderException {
        if (definitionId == null) {
            throw new SBonitaRuntimeException("Unable to evaluate expression without a definitionId");
        }
        final GroovyShell shell = getShell(definitionId);

        GroovyCodeSource gcs = getOrCreateGroovyCodeSource(SCRIPT_KEY + expressionContent.hashCode(),
                expressionContent);
        // parse the groovy source code with cache set to true
        return shell.getClassLoader().parseClass(gcs, true);
    }

    private GroovyCodeSource getOrCreateGroovyCodeSource(String key, String scriptContent) throws SCacheException {
        GroovyCodeSource gcs = (GroovyCodeSource) cacheService.get(GROOVY_SCRIPT_CACHE_NAME, key);

        if (gcs == null) {
            gcs = AccessController
                    .doPrivileged((PrivilegedAction<GroovyCodeSource>) () -> new GroovyCodeSource(scriptContent,
                            generateScriptName(), GroovyShell.DEFAULT_CODE_BASE));
            cacheService.store(GROOVY_SCRIPT_CACHE_NAME, key, gcs);
        }
        return gcs;
    }

    GroovyShell getShell(final Long definitionId) throws SClassLoaderException, SCacheException {
        String key = SHELL_KEY + definitionId;
        GroovyShell shell = (GroovyShell) cacheService.get(GROOVY_SCRIPT_CACHE_NAME, key);
        if (shell == null) {
            ClassLoader classLoader = getClassLoaderForShell(definitionId);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG,
                        "Create a new groovy classloader for " + definitionId + " " + classLoader);
            }
            shell = new GroovyShell(classLoader);
            cacheService.store(GROOVY_SCRIPT_CACHE_NAME, key, shell);
        }
        return shell;
    }

    private ClassLoader getClassLoaderForShell(Long definitionId) throws SClassLoaderException {
        ClassLoader classLoader;
        if (definitionId == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
            //do not has listener, should not happen...
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                IllegalStateException illegalStateException = new IllegalStateException();
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG,
                        "Creating a shell without definition id, might cause issue when reloading classes",
                        illegalStateException);
            }
        } else {
            classLoader = classLoaderService.getClassLoader(identifier(ScopeType.PROCESS, definitionId));
            classLoaderService.addListener(identifier(ScopeType.PROCESS, definitionId), this);
        }
        return classLoader;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context,
            final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException {
        final String expressionContent = expression.getContent();
        final String expressionName = expression.getName();
        try {
            final Binding binding = new Binding(context);
            Long definitionId = (Long) context.get(DEFINITION_ID);
            final Script script = InvokerHelper
                    .createScript(getScriptFromCache(expressionContent, definitionId), binding);
            script.setBinding(binding);
            return coerceResult(getShell(definitionId), script.run(), expression.getReturnType());
        } catch (final MissingPropertyException e) {
            final String property = e.getProperty();
            throw new SExpressionEvaluationException("Expression " + expressionName + " with content = <"
                    + expressionContent + "> depends on " + property
                    + " is neither defined in the script nor in dependencies.", e, expressionName);
        } catch (final GroovyRuntimeException e) {
            throw new SExpressionEvaluationException(e, expressionName);
        } catch (final SCacheException e) {
            throw new SExpressionEvaluationException(
                    "Problem accessing the Script Cache from GroovyScriptExpressionExecutorCacheStrategy.", e,
                    expressionName);
        } catch (final SClassLoaderException e) {
            throw new SExpressionEvaluationException(
                    "Unable to retrieve the correct classloader to execute the groovy script : " + expression, e,
                    expressionName);
        } catch (final Throwable e) {
            //catch throwable because we do not handle contents of scripts
            String message = e.getMessage();
            if (message == null || message.isEmpty()) {
                message = "No message";
            }
            throw new SExpressionEvaluationException(
                    "Groovy script throws an exception of type " + e.getClass() + " with message = " + message
                            + System.getProperty("line.separator") + "Expression : " + expression,
                    e, expressionName);
        }
    }

    @Override
    public void onUpdate(ClassLoader newClassLoader) {
        logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Groovy cache cleared after update on {}", newClassLoader);
        clearCache();
    }

    @Override
    public void onDestroy(ClassLoader oldClassLoader) {
        logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Groovy cache cleared after destroy of {}", oldClassLoader);
        clearCache();
    }

    private void clearCache() {
        try {
            cacheService.clear(GROOVY_SCRIPT_CACHE_NAME);
        } catch (SCacheException e) {
            logger.log(getClass(), TechnicalLogSeverity.ERROR,
                    "error while clearing the cache of the groovy script executor strategy, you might have classloading issue, restart the server if it's the case",
                    e);
        }
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_READ_ONLY_SCRIPT_GROOVY;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context,
            final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState)
            throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        final List<Object> list = new ArrayList<>(expressions.size());
        for (final SExpression expression : expressions) {
            list.add(evaluate(expression, context, resolvedExpressions, containerState));
        }
        return list;
    }

    /**
     * Execute a Groovy expression that coerce the result into the returnType
     *
     * @param shell, the Groovy shell use for script evaluation
     * @param result, the evaluation result
     * @param returnType, expected expression return type
     * @return the result with the expected type or a {@link GroovyCastException} if the coercion fails
     * @throws ClassNotFoundException
     */
    protected Object coerceResult(GroovyShell shell, Object result, String returnType)
            throws ClassNotFoundException, SCacheException {
        if (result == null) {
            return null;
        }
        String resultClassName = result.getClass().getName();
        if (Objects.equals(resultClassName, returnType) // Already in the expected type
                || ReturnTypeChecker.isConvertible(returnType, resultClassName)) { // Bonita specific type conversion
            return result;
        }
        String scriptContent = String.format("result as %s",
                returnType.startsWith("[") ? canonicalClassName(returnType) : returnType);

        GroovyCodeSource gcs = getOrCreateGroovyCodeSource(COERCION_SCRIPT_KEY + returnType, scriptContent);
        Binding binding = new Binding();
        binding.setVariable("result", result);
        Script script = InvokerHelper
                .createScript(shell.getClassLoader().parseClass(gcs, true), binding);
        return script.run();
    }

    private String canonicalClassName(String returnType) throws ClassNotFoundException {
        return Class.forName(returnType).getCanonicalName();
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return false;
    }
}
