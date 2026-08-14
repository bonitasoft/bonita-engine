/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server;

import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.assertj.core.api.SoftAssertions;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-purpose scanner of the Spring MVC REST controllers, checking their class-level
 * {@code @RequestMapping} paths against the {@code SpringRest} servlet mappings of a web.xml.
 * <p>
 * Four rules are enforced:
 * <ul>
 * <li>Every controller must declare a class-level path: one mapping its endpoints at method level
 * only cannot be validated here, and would pass the rules below vacuously.</li>
 * <li><b>Every</b> controller base path must be <i>covered</i> by a servlet mapping, exact or
 * path-prefix. A resource with no mapping at all is simply unreachable (404) - the
 * {@code /API/bpm/processName} failure mode.</li>
 * <li>A base path that has a handler method mapped on the base path itself must additionally be
 * declared as an <b>exact</b> mapping. A path-prefix mapping leaves the resource routable by Tomcat,
 * but makes it depend on the handler-mapping path-matching mode, which already silently broke
 * {@code GET /API/form/mapping} once (see
 * {@code SpringWebConfiguration#configurePathMatch}).</li>
 * <li>A base path whose controller has sub-path endpoints must be covered by a <b>path-prefix</b>
 * mapping: an exact mapping only routes the base path, so {@code /API/some/resource/42} would never
 * reach the DispatcherServlet.</li>
 * </ul>
 * Used by the {@code ServletMappingValidationTest} of both editions - the subscription web.xml is an
 * independent copy of the community one and has to be validated on its own.
 */
public class SpringRestControllerPaths {

    /** Packages component-scanned by {@code SpringWebConfiguration}. */
    public static final String COMMUNITY_CONTROLLERS_PACKAGE = "org.bonitasoft.web.rest.server.api";
    public static final String SUBSCRIPTION_CONTROLLERS_PACKAGE = "com.bonitasoft.web.rest.server.api";

    private SpringRestControllerPaths() {
        // utility class
    }

    /**
     * Asserts the four rules documented on this class for every controller found in the given
     * packages: class-level path declared, base path covered by some pattern, exact pattern for a
     * base-path endpoint, path-prefix pattern for sub-path endpoints.
     *
     * @param webXmlPath the web.xml path, for the failure messages
     * @param servletPatterns the {@code SpringRest} url-patterns declared in that web.xml
     * @param packagesToScan the component-scanned controller packages
     */
    public static void assertServletPatternsDeclared(String webXmlPath, List<String> servletPatterns,
            String... packagesToScan) {
        Set<Class<?>> controllers = scanControllers(packagesToScan);
        assertThat(controllers)
                .withFailMessage(() -> "No controller found: the scanned packages are probably wrong: "
                        + Arrays.toString(packagesToScan))
                .isNotEmpty();

        // soft assertions: one forgotten resource usually breaks several rules at once, and a drift
        // guard is far more useful when a single run reports all of them
        SoftAssertions.assertSoftly(softly -> {
            assertEveryControllerDeclaresABasePath(softly, controllers);
            assertEveryBasePathIsRoutable(softly, webXmlPath, servletPatterns, controllers);
            assertBasePathEndpointsHaveAnExactPattern(softly, webXmlPath, servletPatterns, controllers);
            assertSubPathEndpointsHaveAPrefixPattern(softly, webXmlPath, servletPatterns, controllers);
        });
    }

    /**
     * A controller mapping its endpoints at method level only is legal Spring MVC, but declares no
     * base path for this class to check: it would pass every rule vacuously, so it is rejected instead.
     */
    private static void assertEveryControllerDeclaresABasePath(SoftAssertions softly, Set<Class<?>> controllers) {
        Set<String> withoutBasePath = controllers.stream()
                .filter(controller -> basePathsOf(controller).findAny().isEmpty())
                .map(Class::getName)
                .collect(toCollection(LinkedHashSet::new));

        softly.assertThat(withoutBasePath)
                .withFailMessage(() -> """
                        Controllers without a class-level @RequestMapping path:
                        %s

                        Their url-patterns cannot be validated, so they would pass the rules below \
                        without being checked at all. Declare the resource path on the class and keep \
                        only the sub-paths on the methods."""
                        .formatted(String.join("\n", withoutBasePath)))
                .isEmpty();
    }

    private static void assertEveryBasePathIsRoutable(SoftAssertions softly, String webXmlPath,
            List<String> servletPatterns, Set<Class<?>> controllers) {
        List<String> prefixPatterns = prefixPatternsOf(servletPatterns);
        Set<String> notRoutable = controllers.stream()
                .flatMap(SpringRestControllerPaths::basePathsOf)
                // a templated base path can only be served by a prefix mapping, matching on its literal
                // prefix (e.g. /API/bdm/businessData/{className} -> /API/bdm/businessData/): an exact
                // pattern equal to that prefix would route nothing
                .filter(basePath -> {
                    List<String> candidates = basePath.contains("{") ? prefixPatterns : servletPatterns;
                    String literalPrefix = literalPrefixOf(basePath);
                    return candidates.stream().noneMatch(pattern -> covers(pattern, literalPrefix));
                })
                .collect(toCollection(LinkedHashSet::new));

        softly.assertThat(notRoutable)
                .withFailMessage(() -> """
                        Controller base paths not covered by any <url-pattern> of the SpringRest \
                        servlet-mapping of %s:
                        %s

                        Such a resource is unreachable: the request never gets routed to the Spring \
                        DispatcherServlet. Declare /API/<resource> (and /API/<resource>/* when there \
                        are sub-path endpoints); a templated base path needs the /* pattern.
                        If a path above belongs to a test fixture controller, move that fixture out of \
                        the scanned packages instead: this scan sees the test classpath too."""
                        .formatted(webXmlPath, String.join("\n", notRoutable)))
                .isEmpty();
    }

    private static void assertBasePathEndpointsHaveAnExactPattern(SoftAssertions softly, String webXmlPath,
            List<String> servletPatterns, Set<Class<?>> controllers) {
        Set<String> missingExactPattern = controllers.stream()
                .filter(SpringRestControllerPaths::hasHandlerMethodOnBasePath)
                .flatMap(SpringRestControllerPaths::basePathsOf)
                // a templated base path cannot be expressed as an exact pattern
                .filter(basePath -> !basePath.contains("{"))
                // the internal resources are deliberately served by the single /APISpringInternal/*
                // mapping, reached through urlrewrite forwards only: a per-resource exact pattern there
                // would be noise. SpringWebConfigurationTest is what protects them
                .filter(basePath -> !basePath.startsWith("/" + AbstractRESTController.API_SPRING_INTERNAL))
                .filter(basePath -> !servletPatterns.contains(basePath))
                .collect(toCollection(LinkedHashSet::new));

        softly.assertThat(missingExactPattern)
                .withFailMessage(() -> """
                        Controllers exposing an endpoint on their base path without an exact \
                        <url-pattern> in the SpringRest servlet-mapping of %s:
                        %s

                        A path-prefix pattern (/API/some/resource/*) is not enough for the base path \
                        itself: it makes the resource depend on the handler-mapping path-matching \
                        mode. Declare the exact pattern too (keep the /* one if the resource also has \
                        sub-path endpoints)."""
                        .formatted(webXmlPath, String.join("\n", missingExactPattern)))
                .isEmpty();
    }

    private static void assertSubPathEndpointsHaveAPrefixPattern(SoftAssertions softly, String webXmlPath,
            List<String> servletPatterns, Set<Class<?>> controllers) {
        List<String> prefixPatterns = prefixPatternsOf(servletPatterns);
        Set<String> missingPrefixPattern = controllers.stream()
                .filter(SpringRestControllerPaths::hasHandlerMethodOnSubPath)
                .flatMap(SpringRestControllerPaths::basePathsOf)
                .map(SpringRestControllerPaths::literalPrefixOf)
                .filter(basePath -> prefixPatterns.stream().noneMatch(pattern -> covers(pattern, basePath)))
                .collect(toCollection(LinkedHashSet::new));

        softly.assertThat(missingPrefixPattern)
                .withFailMessage(() -> """
                        Controllers exposing sub-path endpoints without a path-prefix <url-pattern> in \
                        the SpringRest servlet-mapping of %s:
                        %s

                        An exact pattern only routes the base path: sub-path requests \
                        (/API/some/resource/42) would never reach the Spring DispatcherServlet. \
                        Declare /API/<resource>/* as well."""
                        .formatted(webXmlPath, String.join("\n", missingPrefixPattern)))
                .isEmpty();
    }

    /**
     * Note that this scans the whole test runtime classpath, main and test classes alike (and, for the
     * subscription module, the community {@code tests} jar): a test fixture controller declared under
     * one of the scanned packages would be validated like a production one. Fixtures must therefore
     * live outside {@code *.web.rest.server.api}.
     */
    private static Set<Class<?>> scanControllers(String... packagesToScan) {
        Set<Class<?>> controllers = new LinkedHashSet<>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
                false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        for (String packageToScan : packagesToScan) {
            for (BeanDefinition candidate : scanner.findCandidateComponents(packageToScan)) {
                controllers.add(resolveClass(candidate.getBeanClassName()));
            }
        }
        return controllers;
    }

    private static Class<?> resolveClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Scanned controller class not loadable: " + className, e);
        }
    }

    /**
     * All the class-level mapping paths of a controller: a controller can declare several
     * (e.g. {@code ApiExtensionController}), and each of them needs its own servlet mapping.
     */
    private static Stream<String> basePathsOf(Class<?> controller) {
        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(controller, RequestMapping.class);
        return mapping == null ? Stream.empty() : Arrays.stream(mapping.path());
    }

    private static List<String> prefixPatternsOf(List<String> servletPatterns) {
        return servletPatterns.stream().filter(pattern -> pattern.endsWith("/*")).toList();
    }

    /** Part of a base path before its first path variable, trailing separator included. */
    private static String literalPrefixOf(String basePath) {
        int firstVariable = basePath.indexOf('{');
        return firstVariable < 0 ? basePath : basePath.substring(0, firstVariable);
    }

    /**
     * Servlet-spec matching of a url-pattern against a controller base path: an exact pattern has to
     * be equal to it, a path-prefix pattern has to match the path itself or one of its ancestors.
     */
    private static boolean covers(String pattern, String basePath) {
        if (!pattern.endsWith("/*")) {
            return pattern.equals(basePath);
        }
        String prefix = pattern.substring(0, pattern.length() - "/*".length());
        return basePath.equals(prefix) || basePath.startsWith(prefix + "/");
    }

    /**
     * A handler method is mapped on the controller base path when its own mapping declares no path at
     * all, or at least one empty path (e.g. {@code @GetMapping}, {@code @GetMapping("")} or
     * {@code @GetMapping({ "", "/all" })}). Inherited methods count: a subscription controller
     * extending a community one (e.g. {@code FormMappingControllerExt}) exposes the inherited search
     * endpoint on its own base path.
     */
    private static boolean hasHandlerMethodOnBasePath(Class<?> controller) {
        return hasHandlerMethodMatching(controller,
                paths -> paths.length == 0 || Arrays.stream(paths).anyMatch(SpringRestControllerPaths::isEmptyPath));
    }

    /**
     * Conversely, a handler method is mapped on a sub-path as soon as it declares a non-empty path
     * (e.g. {@code @GetMapping("/{id}")}): such a controller needs a path-prefix servlet mapping.
     */
    private static boolean hasHandlerMethodOnSubPath(Class<?> controller) {
        return hasHandlerMethodMatching(controller,
                paths -> Arrays.stream(paths).anyMatch(path -> !isEmptyPath(path)));
    }

    private static boolean hasHandlerMethodMatching(Class<?> controller, Predicate<String[]> pathsPredicate) {
        for (Method method : ReflectionUtils.getAllDeclaredMethods(controller)) {
            RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            if (mapping != null && pathsPredicate.test(mapping.path())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEmptyPath(String path) {
        return path.isEmpty() || "/".equals(path);
    }
}
