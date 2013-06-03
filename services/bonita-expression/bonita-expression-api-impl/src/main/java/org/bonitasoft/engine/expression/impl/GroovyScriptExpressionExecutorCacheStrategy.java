/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.cache.CacheException;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.classloader.ClassLoaderException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.expression.NonEmptyContentExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhao na
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class GroovyScriptExpressionExecutorCacheStrategy extends NonEmptyContentExpressionExecutorStrategy {

    private static final String GROOVY_SCRIPT_CACHE_NAME = "GROOVY_SCRIPT_CACHE_NAME";

    private static final String SCRIPT_KEY = "SCRIPT_";

    private static final String SHELL_KEY = "SHELL_";

    private final CacheService cacheService;

    private final ClassLoaderService classLoaderService;

    public GroovyScriptExpressionExecutorCacheStrategy(final CacheService cacheService, final ClassLoaderService classLoaderService) {
        this.cacheService = cacheService;
        this.classLoaderService = classLoaderService;
    }

    private Script getScriptFromCache(final String expressionContent, final Long definitionId) throws CacheException, ClassLoaderException {
        final GroovyShell shell = getShell(definitionId);
        Script script = (Script) cacheService.get(GROOVY_SCRIPT_CACHE_NAME, SCRIPT_KEY + definitionId + expressionContent.hashCode());
        if (script == null) {
            script = shell.parse(expressionContent);
            cacheService.store(GROOVY_SCRIPT_CACHE_NAME, SCRIPT_KEY + definitionId + expressionContent.hashCode(), script);
        }
        return script;
    }

    private GroovyShell getShell(final Long definitionId) throws ClassLoaderException, CacheException {
        GroovyShell shell = (GroovyShell) cacheService.get(GROOVY_SCRIPT_CACHE_NAME, SHELL_KEY + definitionId);
        if (shell == null) {
            ClassLoader classLoader;
            if (definitionId != null) {
                classLoader = classLoaderService.getLocalClassLoader(DEFINITION_TYPE, definitionId);
            } else {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            shell = new GroovyShell(classLoader);
            cacheService.store(GROOVY_SCRIPT_CACHE_NAME, SHELL_KEY + definitionId, shell);
        }
        return shell;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionEvaluationException {
        final String expressionContent = expression.getContent();
        try {
            final Script script = getScriptFromCache(expressionContent, (Long) dependencyValues.get(DEFINITION_ID));
            final Binding binding = new Binding(dependencyValues);
            script.setBinding(binding);
            return script.evaluate(expressionContent);// .evaluate(expressionContent);run()
        } catch (final MissingPropertyException e) {
            final String property = e.getProperty();
            final StringBuilder builder = new StringBuilder("Expression ");
            builder.append(expression.getName()).append(" with content: ").append(expressionContent).append(" depends on ").append(property)
                    .append(" is neither defined in the script nor in dependencies");
            throw new SExpressionEvaluationException(builder.toString(), e);
        } catch (final GroovyRuntimeException e) {
            throw new SExpressionEvaluationException(e);
        } catch (final CacheException e) {
            throw new SExpressionEvaluationException("Problem accessing the Script Cache from GroovyScriptExpressionExecutorCacheStrategy", e);
        } catch (final ClassLoaderException e) {
            throw new SExpressionEvaluationException("Unable to retrieve the correct classloader to execute the groovy script: " + expression, e);
        } catch (final Throwable e) {
            throw new SExpressionEvaluationException("Script throws an exception" + expression, e);
        }
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_READ_ONLY_SCRIPT_GROOVY;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionDependencyMissingException, SExpressionEvaluationException {
        final List<Object> list = new ArrayList<Object>(expressions.size());
        for (final SExpression expression : expressions) {
            list.add(evaluate(expression, dependencyValues, resolvedExpressions));
        }
        return list;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return false;
    }

}
