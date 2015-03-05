/**
 * Copyright (C) 2011-2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.expression.impl;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.codehaus.groovy.runtime.InvokerHelper;

import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

/**
 * @author Zhao na
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class GroovyScriptExpressionExecutorCacheStrategy extends AbstractGroovyScriptExpressionExecutorStrategy {

    public static final String GROOVY_SCRIPT_CACHE_NAME = "GROOVY_SCRIPT_CACHE_NAME";

    public static final String SCRIPT_KEY = "SCRIPT_";

    public static final String SHELL_KEY = "SHELL_";

    private final CacheService cacheService;

    private final ClassLoaderService classLoaderService;

    private final TechnicalLoggerService logger;

    private final boolean debugEnabled;

    private static int counter;

    public GroovyScriptExpressionExecutorCacheStrategy(final CacheService cacheService, final ClassLoaderService classLoaderService,
            final TechnicalLoggerService logger) {
        this.cacheService = cacheService;
        this.classLoaderService = classLoaderService;
        this.logger = logger;
        debugEnabled = logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG);
    }

    protected synchronized String generateScriptName() {
        return "BScript" + (++counter) + ".groovy";
    }

    Class getScriptFromCache(final String expressionContent, final Long definitionId) throws SCacheException, SClassLoaderException {
        final GroovyShell shell = getShell(definitionId);
        /*
         * We use the current thread id is the key because Scripts are not thread safe (because of binding)
         * This way we store one script for each thread, it is like a thread local cache.
         */
        final String key = SCRIPT_KEY + expressionContent.hashCode();

        GroovyCodeSource gcs = (GroovyCodeSource) cacheService.get(GROOVY_SCRIPT_CACHE_NAME, key);

        if (gcs == null) {
            gcs = AccessController.doPrivileged(new PrivilegedAction<GroovyCodeSource>() {

                public GroovyCodeSource run() {
                    return new GroovyCodeSource(expressionContent, generateScriptName(), GroovyShell.DEFAULT_CODE_BASE);
                }
            });
            cacheService.store(GROOVY_SCRIPT_CACHE_NAME, key, gcs);
        }
        // parse the groovy source code with cache set to true
        return shell.getClassLoader().parseClass(gcs, true);
    }

    GroovyShell getShell(final Long definitionId) throws SClassLoaderException, SCacheException {
        String key = null;
        GroovyShell shell = null;
        if (definitionId != null) {
            key = SHELL_KEY + definitionId;
            shell = (GroovyShell) cacheService.get(GROOVY_SCRIPT_CACHE_NAME, key);
        }
        if (shell == null) {
            ClassLoader classLoader;
            if (definitionId == null) {
                if (debugEnabled) {
                    logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "no definitionId provided ");
                }
                classLoader = Thread.currentThread().getContextClassLoader();
            } else {
                classLoader = classLoaderService.getLocalClassLoader(DEFINITION_TYPE, definitionId);
                cacheService.store(GROOVY_SCRIPT_CACHE_NAME, key, shell);
            }
            if (debugEnabled) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Create a new groovy classloader for " + definitionId + " " + classLoader);
            }
            shell = new GroovyShell(classLoader);
        }
        return shell;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException {
        final String expressionContent = expression.getContent();
        final String expressionName = expression.getName();
        try {
            final Binding binding = new Binding(context);
            final Script script = InvokerHelper.createScript(getScriptFromCache(expressionContent, (Long) context.get(DEFINITION_ID)), binding);
            script.setBinding(binding);
            return script.run();
        } catch (final MissingPropertyException e) {
            final String property = e.getProperty();
            final StringBuilder builder = new StringBuilder("Expression ");
            builder.append(expressionName).append(" with content = <").append(expressionContent).append("> depends on ").append(property)
                    .append(" is neither defined in the script nor in dependencies.");
            throw new SExpressionEvaluationException(builder.toString(), e, expressionName);
        } catch (final GroovyRuntimeException e) {
            throw new SExpressionEvaluationException(e, expressionName);
        } catch (final SCacheException e) {
            throw new SExpressionEvaluationException("Problem accessing the Script Cache from GroovyScriptExpressionExecutorCacheStrategy.", e, expressionName);
        } catch (final SClassLoaderException e) {
            throw new SExpressionEvaluationException("Unable to retrieve the correct classloader to execute the groovy script : " + expression, e,
                    expressionName);
        } catch (final Exception e) {
            String message = e.getMessage();
            if (message == null || message.isEmpty()) {
                message = "No message";
            }
            throw new SExpressionEvaluationException("Groovy script throws an exception of type " + e.getClass() + " with message = " + message
                    + System.getProperty("line.separator") + "Expression : " + expression, e, expressionName);
        }
    }

}
