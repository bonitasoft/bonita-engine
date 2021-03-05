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

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.expression.ExpressionExecutorStrategy.DEFINITION_ID;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import groovy.lang.GroovyShell;
import org.bonitasoft.engine.bpm.contract.FileInputValue;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.cache.CacheConfiguration;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.cache.ehcache.EhCacheCacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;
import org.bonitasoft.engine.expression.model.builder.impl.SExpressionBuilderFactoryImpl;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.codehaus.groovy.runtime.typehandling.GroovyCastException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GroovyScriptExpressionExecutorCacheStrategyTest {

    @Mock
    private ClassLoaderService classLoaderService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private ReadSessionAccessor sessionAccessor;

    @Mock
    private CacheConfiguration defaultCacheConfiguration;

    private EhCacheCacheService cacheService;

    private GroovyScriptExpressionExecutorCacheStrategy groovyScriptExpressionExecutorCacheStrategy;

    private final static String diskStorePath = IOUtil.TMP_DIRECTORY + File.separator
            + GroovyScriptExpressionExecutorCacheStrategyTest.class.getSimpleName();
    private Class script2;
    private Map<String, Object> context;

    @Before
    public void setup() throws Exception {
        final CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName("GROOVY_SCRIPT_CACHE_NAME");
        final List<CacheConfiguration> cacheConfigurations = Collections.singletonList(cacheConfiguration);
        cacheService = new EhCacheCacheService(logger, cacheConfigurations, defaultCacheConfiguration, diskStorePath,
                1);
        cacheService.start();
        groovyScriptExpressionExecutorCacheStrategy = new GroovyScriptExpressionExecutorCacheStrategy(cacheService,
                classLoaderService, logger);
        doReturn(GroovyScriptExpressionExecutorCacheStrategyTest.class.getClassLoader()).when(classLoaderService)
                .getClassLoader(any());
        context = new HashMap<>();
        context.put(DEFINITION_ID, 123456789L);
    }

    @After
    public void teardown() {
        cacheService.stop();
    }

    @AfterClass
    public static void cleanupClass() throws IOException {
        IOUtil.deleteDir(new File(diskStorePath));
    }

    @Test
    public void should_getShell_return_a_shell_for_each_definition() throws Exception {
        // given

        // when
        final GroovyShell shell1 = groovyScriptExpressionExecutorCacheStrategy.getShell(12L);
        final GroovyShell shell2 = groovyScriptExpressionExecutorCacheStrategy.getShell(13L);

        // then
        assertThat(shell1).isNotNull();
        assertThat(shell2).isNotNull();
        assertThat(shell1).isNotEqualTo(shell2);
    }

    @Test
    public void should_update_on_classloader_listener_clear_shell_cache() throws Exception {
        // given

        // when
        final GroovyShell shell1 = groovyScriptExpressionExecutorCacheStrategy.getShell(12l);
        groovyScriptExpressionExecutorCacheStrategy.onUpdate(null);
        final GroovyShell shell2 = groovyScriptExpressionExecutorCacheStrategy.getShell(12l);

        // then
        assertThat(shell1).isNotNull();
        assertThat(shell2).isNotNull();
        assertThat(shell1).isNotEqualTo(shell2);
    }

    @Test
    public void should_destroy_on_classloader_listener_clear_shell_cache() throws Exception {
        // given

        // when
        final GroovyShell shell1 = groovyScriptExpressionExecutorCacheStrategy.getShell(12l);
        groovyScriptExpressionExecutorCacheStrategy.onDestroy(null);
        final GroovyShell shell2 = groovyScriptExpressionExecutorCacheStrategy.getShell(12l);

        // then
        assertThat(shell1).isNotNull();
        assertThat(shell2).isNotNull();
        assertThat(shell1).isNotEqualTo(shell2);
    }

    @Test
    public void should_getShell_return_same_shell_for_1_definition() throws Exception {
        // when
        final GroovyShell shell1 = groovyScriptExpressionExecutorCacheStrategy.getShell(12L);
        final GroovyShell shell2 = groovyScriptExpressionExecutorCacheStrategy.getShell(12L);

        // then
        assertThat(shell1).isNotNull();
        assertThat(shell1).isEqualTo(shell2);
    }

    @Test
    public void should_getScriptFromCache_return_a_the_same_script_class_if_in_same_definition() throws Exception {
        // when
        final Class script1 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12L);
        final Class script2 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12L);

        // then
        assertThat(script1).isNotNull();
        assertThat(script1).isEqualTo(script2);

    }

    @Test
    public void should_getScriptFromCache_should_cache_only_once_when_on_different_threads() throws Exception {
        // when
        final Class script1 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12L);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    script2 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12L);
                } catch (SCacheException | SClassLoaderException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();

        // then
        assertThat(script1).isNotNull();
        assertThat(script2).isNotNull();
        assertThat(script1).isEqualTo(script2);

    }

    @Test
    public void should_getScriptFromCache_return_different_script_if_different_definition() throws Exception {
        // when
        final Class script1 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12L);
        final Class script2 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 13L);

        // then
        assertThat(script1).isNotNull();
        assertThat(script2).isNotNull();
        assertThat(script1).isNotEqualTo(script2);
    }

    @Test
    public void should_getScriptFromCache_return_different_script_if_content_is_different_definition()
            throws Exception {
        // when
        final Class script1 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent1", 12L);
        final Class script2 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent2", 12L);

        // then
        assertThat(script1).isNotNull();
        assertThat(script2).isNotNull();
        assertThat(script1).isNotEqualTo(script2);
    }

    @Test(expected = SBonitaRuntimeException.class)
    public void should_not_put_in_cache_script_without_definition_id() throws Exception {

        // when
        groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent1", null);

        // then
        //exception
    }

    @Test
    public void should_evaluate_return_the_evaluation() throws Exception {
        //given
        final SExpressionImpl expression = new SExpressionImpl("myExpr", "'toto'", null, "java.lang.String", null,
                Collections.emptyList());
        // when
        final Object evaluate = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression, context,
                Collections.emptyMap(),
                ContainerState.ACTIVE);

        // then
        assertThat(evaluate).isEqualTo("toto");
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void should_evaluate_throw_SExpressionEvaluationException_when_script_throws_Error() throws Exception {
        //given
        final SExpressionImpl expression = new SExpressionImpl("myExpr", "throw new java.lang.NoClassDefFoundError()",
                null, "java.lang.String", null,
                Collections.emptyList());
        // when
        groovyScriptExpressionExecutorCacheStrategy.evaluate(expression, context,
                Collections.emptyMap(), ContainerState.ACTIVE);

        // then
        //exception
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void should_evaluate_throw_SExpressionEvaluationException_when_script_throws_Throwable() throws Exception {
        //given
        final SExpressionImpl expression = new SExpressionImpl("myExpr", "throw new java.lang.Throwable()", null,
                "java.lang.String", null,
                Collections.emptyList());
        // when
        groovyScriptExpressionExecutorCacheStrategy.evaluate(expression, context,
                Collections.emptyMap(), ContainerState.ACTIVE);
        // then
        //exception
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void evaluation_with_DefaultGroovyMethod_size_should_return_4()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "import static org.codehaus.groovy.runtime.DefaultGroovyMethods.*\n"
                + "size('test'.toCharArray())";
        SExpression expression = integerExpression(content);
        Object value = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression,
                singletonMap(DEFINITION_ID, 42L), emptyMap(), null);
        assertThat(value)
                .isInstanceOf(Integer.class)
                .isEqualTo(4);
    }

    @Test
    public void evaluation_with_string_size_should_return_4()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "new StringBuffer('test').size()";
        SExpression expression = integerExpression(content);
        Object value = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression,
                singletonMap(DEFINITION_ID, 42L), emptyMap(), null);
        assertThat(value)
                .isInstanceOf(Integer.class)
                .isEqualTo(4);
    }

    private static SExpressionBuilder expressionBuilder() {
        return new SExpressionBuilderFactoryImpl().createNewInstance().setName("test");
    }

    private static SExpression integerExpression(String content) throws SInvalidExpressionException {
        return expressionBuilder().setContent(content).setReturnType(Integer.class.getName()).done();
    }

    @Test
    public void evaluation_with_jsonBuilder_toString_should_work_on_java_8_and_java_11()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "import groovy.json.JsonBuilder\n"
                + "new JsonBuilder('hello').toString()";
        SExpression expression = expressionBuilder().setContent(content).setReturnType(String.class.getName()).done();
        Object value = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression,
                singletonMap(DEFINITION_ID, 42L), emptyMap(), null);
        assertThat(value)
                .isInstanceOf(String.class)
                .isEqualTo("\"hello\"");
    }

    @Test
    public void evaluation_should_coerce_to_string_return_type()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "1";

        SExpression expression = expressionBuilder().setContent(content).setReturnType(String.class.getName()).done();
        Object stringValue = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression,
                singletonMap(DEFINITION_ID, 42L), emptyMap(),
                null);
        assertThat(stringValue)
                .isInstanceOf(String.class)
                .isEqualTo("1");
    }

    @Test
    public void evaluation_should_coerce_groovy_string_to_string_return_type()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "\"\"\"Hello ${firstName}\"\"\"";

        Map<String, Object> context = new HashMap<>();
        context.put(DEFINITION_ID, 42L);
        context.put("firstName", "Romain");
        SExpression expression = expressionBuilder().setContent(content).setReturnType(String.class.getName()).done();
        Object stringValue = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression, context, emptyMap(),
                null);
        assertThat(stringValue)
                .isInstanceOf(String.class)
                .isEqualTo("Hello Romain");
    }

    @Test
    public void evaluation_should_coerce_to_integer_return_type()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "1";

        SExpression expression = expressionBuilder().setContent(content).setReturnType(Integer.class.getName()).done();
        Object intergerValue = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression,
                singletonMap(DEFINITION_ID, 42L), emptyMap(),
                null);
        assertThat(intergerValue)
                .isInstanceOf(Integer.class)
                .isEqualTo(1);
    }

    @Test
    public void evaluation_should_coerce_to_double_return_type()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "1";

        SExpression expression = expressionBuilder().setContent(content).setReturnType(Double.class.getName()).done();
        Object doubleValue = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression,
                singletonMap(DEFINITION_ID, 42L), emptyMap(),
                null);
        assertThat(doubleValue)
                .isInstanceOf(Double.class)
                .isEqualTo(1d);
    }

    @Test
    public void evaluation_should_coerce_thruthy_expression_to_boolean_return_type()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String thruthyContent = "1"; // 1 is truthy

        SExpression expression = expressionBuilder().setContent(thruthyContent).setReturnType(Boolean.class.getName())
                .done();
        Object value = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression,
                singletonMap(DEFINITION_ID, 42L), emptyMap(), null);
        assertThat(value)
                .isInstanceOf(Boolean.class)
                .isEqualTo(true);

    }

    @Test
    public void evaluation_should_coerce_falsy_expression_to_boolean_return_type()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String falsyContent = "0"; // 0 is falsy

        SExpression expression = expressionBuilder().setContent(falsyContent).setReturnType(Boolean.class.getName())
                .done();
        Object value = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression,
                singletonMap(DEFINITION_ID, 42L), emptyMap(), null);
        assertThat(value)
                .isInstanceOf(Boolean.class)
                .isEqualTo(false);

    }

    @Test
    public void evaluation_should_coerce_list_expression_to_array_return_type()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String falsyContent = "[1,2,3]";

        SExpression expression = expressionBuilder().setContent(falsyContent).setReturnType(String[].class.getName())
                .done();
        Object value = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression,
                singletonMap(DEFINITION_ID, 42L), emptyMap(), null);
        assertThat(value)
                .isInstanceOf(String[].class)
                .isEqualTo(new String[] { "1", "2", "3" });

    }

    @Test
    public void evaluation_should_not_coerce_FileInputValue_to_Document_return_type()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "document";

        SExpression expression = expressionBuilder().setContent(content).setReturnType(Document.class.getName())
                .done();
        Map<String, Object> context = new HashMap<>();
        FileInputValue fileInputValue = new FileInputValue("someDoc", null);
        context.put("document", fileInputValue);
        context.put(DEFINITION_ID, 42L);
        Object value = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression, context, emptyMap(), null);
        assertThat(value)
                .isInstanceOf(FileInputValue.class)
                .isEqualTo(fileInputValue);
    }

    @Test
    public void evaluation_should_not_coerce_FileInputValue_to_DocumentValue_return_type()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "documentValue";

        SExpression expression = expressionBuilder().setContent(content).setReturnType(DocumentValue.class.getName())
                .done();
        Map<String, Object> context = new HashMap<>();
        FileInputValue fileInputValue = new FileInputValue("someDoc", null);
        context.put("documentValue", fileInputValue);
        context.put(DEFINITION_ID, 42L);
        Object value = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression, context, emptyMap(), null);
        assertThat(value)
                .isInstanceOf(FileInputValue.class)
                .isEqualTo(fileInputValue);
    }

    @Test
    public void evaluation_should_not_coerce_null_result()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "null";

        SExpression expression = expressionBuilder().setContent(content).setReturnType(String.class.getName())
                .done();
        Object value = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression,
                singletonMap(DEFINITION_ID, 42L), emptyMap(), null);
        assertThat(value).isNull();
    }

    @Test
    public void evaluation_should_throw_a_GroovyCastException_when_coercing_incompatible_types()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String notAList = "[1]"; // cannot be casted into Long

        SExpression expression = expressionBuilder().setContent(notAList).setReturnType(Long.class.getName()).done();

        expectedException.expectCause(is(instanceOf(GroovyCastException.class)));
        groovyScriptExpressionExecutorCacheStrategy.evaluate(expression, singletonMap(DEFINITION_ID, 42L), emptyMap(),
                null);
    }

    @Test
    public void evaluation_should_coerce_string_into_long()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "'123'";

        SExpression expression = expressionBuilder().setContent(content).setReturnType(Long.class.getName()).done();

        Object value = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression,
                singletonMap(DEFINITION_ID, 42L), emptyMap(), null);
        assertThat(value).isEqualTo(123L);
    }

    @Test
    public void evaluation_should_coerce_double_into_long()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "123d";

        SExpression expression = expressionBuilder().setContent(content).setReturnType(Long.class.getName()).done();

        Object value = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression,
                singletonMap(DEFINITION_ID, 42L), emptyMap(), null);
        assertThat(value).isEqualTo(123L);
    }

    @Test
    public void evaluation_should_throw_a_NumberFormatException_when_string_cannot_be_formatted()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String notAList = "'123a'"; // cannot be coerce into Long

        SExpression expression = expressionBuilder().setContent(notAList).setReturnType(Long.class.getName()).done();

        expectedException.expectCause(is(instanceOf(NumberFormatException.class)));
        groovyScriptExpressionExecutorCacheStrategy.evaluate(expression, singletonMap(DEFINITION_ID, 42L), emptyMap(),
                null);
    }
}
