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
package org.bonitasoft.engine.bpm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * BPA-833: the standing guard for the whole class of defect, as a fixture so that every module able to see a
 * meaningful slice of the engine can run it.
 * <p>
 * The JavaBeans specification sanctions an {@code isX()} accessor only for a <em>primitive</em> {@code boolean}.
 * Groovy 4 enforces that, so an {@code isX()} returning the wrapper {@code Boolean} is not a property getter: the
 * {@code obj.x} shorthand used in process expressions falls back to direct field access, which fails on any subclass
 * receiver - and business data, data instances and Hibernate-managed entities are always subclasses in practice.
 * {@code java.beans.Introspector} refuses such an accessor for the same reason, so the property is invisible to
 * Spring {@code BeanUtils}, commons-beanutils, EL/SpEL and form binding as well.
 * <p>
 * Every wrapper-{@code Boolean} {@code isX()} therefore needs a {@code getX()} companion, or the attribute should
 * declare a primitive {@code boolean} instead. This walks compiled bytecode rather than grepping sources, because the
 * companion can come from three places a grep cannot see: a declaration, an inherited interface default method, or
 * Lombok.
 * <h2>Where the classes come from</h2>
 * The scan reads the classpath named by the {@value #CLASSPATH_PROPERTY} system property, which the
 * {@code bonita-boolean-accessor-scan} plugin sets from each {@code Test} task's own classpath. It deliberately does
 * not default to
 * {@code java.class.path}: Gradle replaces that with a manifest-only "pathing jar" once the command line exceeds the
 * OS limit, and a scan reading it would then quietly collapse to nothing - which is the failure mode BPA-833 came
 * from, a scan scoped too narrowly reporting clean.
 */
public final class BooleanAccessorPairingScan {

    /** Set by each consuming module's {@code test} task; see the class javadoc for why there is no fallback. */
    public static final String CLASSPATH_PROPERTY = "bonita.accessorScan.classpath";

    /**
     * Accessors declared in artifacts this repository only consumes, so they cannot be paired here.
     * {@code SimpleField} and {@code RelationField} extend {@code Field}, so reading {@code simpleField.nullable}
     * from Groovy already has the subclass receiver that triggers the defect. Nothing in this repository reads them
     * as properties, so there is no live defect - the exposure is customer code inspecting a BDM model from Groovy.
     * Tracked against the {@code bonita-artifacts-model} project.
     * <p>
     * Keyed on the <em>declaring</em> class, so inheriting subclasses need no entry of their own and a new subclass
     * upstream cannot break the guard. Entries may disappear when that project repairs them: this is an allowlist of
     * what is tolerated, never an assertion that the defects still exist - coupling a build here to a defect owned
     * elsewhere would turn an upstream fix into a red build.
     */
    private static final Set<String> TOLERATED_UNPAIRED_OUTSIDE_THIS_REPOSITORY = Set.of(
            "org.bonitasoft.engine.bdm.model.field.Field.isNullable",
            "org.bonitasoft.engine.bdm.model.field.Field.isCollection");

    private static final Pattern CLASS_SUFFIX = Pattern.compile("\\.class$");

    private static final String MULTI_RELEASE_PREFIX = "META-INF/versions/";

    /** A binary class name: dot-separated identifiers, with {@code $} allowed for nested types. */
    private static final Pattern CLASS_NAME = Pattern
            .compile("[\\p{L}_$][\\p{L}\\p{N}_$]*(\\.[\\p{L}_$][\\p{L}\\p{N}_$]*)*");

    private BooleanAccessorPairingScan() {
    }

    /**
     * @param scannedClassNames every Bonita production class the scan examined
     * @param unpaired {@code declaringClass.accessorName} for each wrapper-{@code Boolean} {@code isX()} with no
     *        {@code getX()} companion, keyed on the <em>declaring</em> class so an inheriting subclass does not
     *        produce an entry of its own
     * @param uninspectable classes that could not be loaded or whose signatures could not be resolved
     */
    public record Result(List<String> scannedClassNames, Set<String> unpaired, List<Uninspectable> uninspectable) {}

    /**
     * A class the scan could not examine.
     *
     * @param className the Bonita class that dropped out
     * @param missingType the type whose absence caused it, or {@code null} when the failure was not a missing class -
     *        which is itself an anomaly, since it means the scan lost a class for a reason nobody has accounted for
     * @param cause the throwable's own description, kept for the failure message
     */
    public record Uninspectable(String className, String missingType, String cause) {

        @Override
        public String toString() {
            return className + " (missing " + missingType + "; " + cause + ")";
        }
    }

    /**
     * Runs the scan and asserts the invariant, tolerating only {@link #TOLERATED_UNPAIRED_OUTSIDE_THIS_REPOSITORY}.
     *
     * @param minimumClassesScanned a floor proving the classpath actually resolved, rather than the scan reporting
     *        clean because it found nothing
     * @param classesProvingReach types the scanning module only consumes, asserted present so a scan that quietly
     *        narrows to the local output fails here. Name the types the guard is supposed to protect, not convenient
     *        ones: the earlier version of this guard reported clean while unable to see two of the four types BPA-833
     *        fixed, so "the scan found nothing" and "the scan could not see it" have to be distinguishable.
     */
    public static void assertEveryWrapperBooleanIsAccessorIsPaired(final int minimumClassesScanned,
            final List<String> classesProvingReach) throws IOException {
        final Result result = scan();

        assertThat(result.scannedClassNames())
                .as("Bonita production classes to scan. A count far below expectation usually means the"
                        + " %s system property is unset or resolved to nothing - the scan does not fall back to"
                        + " java.class.path, because Gradle may replace that with a manifest-only pathing jar",
                        CLASSPATH_PROPERTY)
                .hasSizeGreaterThan(minimumClassesScanned);
        assertThat(result.scannedClassNames())
                .as("the scan must keep reaching consumed artifacts, not only this module's own output")
                .containsAll(classesProvingReach);
        assertThat(result.unpaired())
                .as("a wrapper-Boolean isX() with no getX() companion is unreadable as a JavaBeans or Groovy"
                        + " property - add the companion, or declare the attribute as a primitive boolean")
                .allMatch(TOLERATED_UNPAIRED_OUTSIDE_THIS_REPOSITORY::contains,
                        "declared outside this module's reach and tolerated");
        // A class dropping out because an optional third-party API is absent from the test runtime classpath is
        // expected and harmless: jakarta.servlet and the SAP JCo client are compileOnly in the engine, so the
        // servlet and SAP connector classes cannot be loaded here. A class dropping out because a *Bonita* type is
        // missing is not harmless - that is the scan losing sight of our own code, which is the failure mode this
        // guard exists to prevent, so it is the rule asserted rather than a count. Stated as a rule so that adding
        // an optional dependency upstream does not redden an unrelated module's build.
        assertThat(result.uninspectable())
                .as("a class may only drop out of the scan because a non-Bonita, optional dependency is absent."
                        + " An entry here whose missing type is a Bonita class means the scan stopped seeing our own"
                        + " code and would report clean because it stopped looking. Entries: %s",
                        result.uninspectable())
                .allSatisfy(dropped -> {
                    assertThat(dropped.missingType())
                            .as("no missing type could be derived for %s, so the reason it dropped out is"
                                    + " unaccounted for", dropped)
                            .isNotNull();
                    assertThat(isBonitaClass(dropped.missingType()))
                            .as("%s dropped out because a Bonita type is missing", dropped)
                            .isFalse();
                });
    }

    public static Result scan() throws IOException {
        final Set<String> unpaired = new TreeSet<>();
        final List<Uninspectable> uninspectable = new ArrayList<>();
        final List<String> classNames = bonitaProductionClassNames();
        for (final String className : classNames) {
            final Class<?> type = loadOrRecord(className, uninspectable);
            if (type == null) {
                continue;
            }
            for (final Method accessor : wrapperBooleanIsAccessorsOf(type, uninspectable)) {
                if (!hasCompanionGetter(type, accessor)) {
                    unpaired.add(accessor.getDeclaringClass().getName() + "." + accessor.getName());
                }
            }
        }
        return new Result(classNames, unpaired, uninspectable);
    }

    private static List<String> bonitaProductionClassNames() throws IOException {
        final String classpath = System.getProperty(CLASSPATH_PROPERTY);
        if (classpath == null || classpath.isBlank()) {
            throw new IllegalStateException("The " + CLASSPATH_PROPERTY + " system property must be set from the"
                    + " module's test runtime classpath. Add to the module's build.gradle:\n"
                    + "    test { systemProperty '" + CLASSPATH_PROPERTY
                    + "', sourceSets.test.runtimeClasspath.asPath }");
        }
        final List<String> classNames = new ArrayList<>();
        for (final String entry : classpath.split(File.pathSeparator)) {
            final Path path = Path.of(entry);
            if (isTestOutput(entry) || !Files.exists(path)) {
                continue;
            }
            if (Files.isDirectory(path)) {
                classNames.addAll(classNamesInDirectory(path));
            } else if (entry.endsWith(".jar")) {
                classNames.addAll(classNamesInJar(path));
            }
        }
        return classNames.stream().filter(BooleanAccessorPairingScan::isBonitaClass).distinct().toList();
    }

    /**
     * Test and fixture output is excluded because a fixture subclassing an external type inherits its unpaired
     * accessors without any process expression ever seeing them. {@code testFixtures} is named explicitly rather than
     * left to the {@code /classes/java/test} prefix, which happens to match it too: relying on that collision would
     * silently stop excluding fixtures if either directory were ever renamed.
     */
    private static boolean isTestOutput(final String classpathEntry) {
        final String normalised = classpathEntry.replace(File.separatorChar, '/');
        return normalised.contains("/classes/java/test") || normalised.contains("/classes/java/testFixtures")
                || normalised.contains("/classes/groovy/test")
                // test artifacts pulled in by classifier, e.g. bonita-business-object-model-*-tests.jar, whose
                // fixtures subclass external model types and so inherit their unpaired accessors
                || normalised.endsWith("-tests.jar") || normalised.endsWith("-test-fixtures.jar");
    }

    private static boolean isBonitaClass(final String className) {
        return className.startsWith("org.bonitasoft.") || className.startsWith("com.bonitasoft.");
    }

    private static List<String> classNamesInDirectory(final Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(path -> path.toString().endsWith(".class"))
                    .map(path -> toClassName(root.relativize(path).toString()))
                    .toList();
        }
    }

    private static List<String> classNamesInJar(final Path jar) throws IOException {
        final List<String> classNames = new ArrayList<>();
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            for (final Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                final String name = entries.nextElement().getName();
                if (name.endsWith(".class")) {
                    classNames.add(toClassName(stripMultiReleasePrefix(name)));
                }
            }
        }
        return classNames;
    }

    /**
     * A multi-release jar holds version-specific copies under {@code META-INF/versions/N/}. Left prefixed they would
     * fail the {@code org.bonitasoft.} filter and drop out silently, so the prefix is removed and the class is
     * examined - the duplicate it creates is removed by {@code distinct()}.
     */
    private static String stripMultiReleasePrefix(final String entryName) {
        if (!entryName.startsWith(MULTI_RELEASE_PREFIX)) {
            return entryName;
        }
        final int afterVersion = entryName.indexOf('/', MULTI_RELEASE_PREFIX.length());
        return afterVersion < 0 ? entryName : entryName.substring(afterVersion + 1);
    }

    private static String toClassName(final String entryName) {
        return CLASS_SUFFIX.matcher(entryName.replace(File.separatorChar, '.').replace('/', '.')).replaceFirst("");
    }

    private static Class<?> loadOrRecord(final String className, final List<Uninspectable> uninspectable) {
        try {
            return Class.forName(className, false, BooleanAccessorPairingScan.class.getClassLoader());
        } catch (final Throwable unloadable) {
            uninspectable.add(new Uninspectable(className, missingTypeOf(unloadable), String.valueOf(unloadable)));
            return null;
        }
    }

    /**
     * {@link NoClassDefFoundError} and {@link ClassNotFoundException} usually carry the absent type as their whole
     * message, in internal form for the former - but not always: {@code "Bar (wrong name: bar)"} and
     * {@code "Could not initialize class X"} are both possible. Taking such a message whole gets the tolerance
     * decision wrong in both directions - the first would start with {@code org.bonitasoft.} and fail the build
     * naming the wrong cause, the second would not and would tolerate a genuinely broken Bonita class. So only a
     * value actually shaped like a class name is accepted; anything else yields {@code null}, which the assertion
     * reports as an anomaly rather than tolerating.
     */
    static String missingTypeOf(final Throwable failure) {
        if (!(failure instanceof NoClassDefFoundError || failure instanceof ClassNotFoundException)) {
            return null;
        }
        final String message = failure.getMessage();
        if (message == null || message.isBlank()) {
            return null;
        }
        final String candidate = message.replace('/', '.');
        return CLASS_NAME.matcher(candidate).matches() ? candidate : null;
    }

    static List<Method> wrapperBooleanIsAccessorsOf(final Class<?> type,
            final List<Uninspectable> uninspectable) {
        try {
            return Stream.of(type.getMethods())
                    .filter(method -> method.getParameterCount() == 0)
                    // a JavaBeans accessor is an instance method; a static Boolean isSomething() is not one and must
                    // not be asked for a companion, which would redden this guard for something it does not govern
                    .filter(method -> !Modifier.isStatic(method.getModifiers()))
                    // JavaBeans requires an upper-case character after "is", so a method such as
                    // Boolean isolate() is not an accessor and must not be asked for a getolate() companion
                    .filter(method -> method.getName().startsWith("is") && method.getName().length() > 2
                            && Character.isUpperCase(method.getName().charAt(2)))
                    .filter(method -> Boolean.class == method.getReturnType())
                    .filter(method -> !method.isSynthetic())
                    .toList();
        } catch (final Throwable unresolvableSignature) {
            uninspectable.add(new Uninspectable(type.getName(), missingTypeOf(unresolvableSignature),
                    String.valueOf(unresolvableSignature)));
            return List.of();
        }
    }

    /**
     * Accepts a companion declared on the type or inherited from any supertype, since either makes the property
     * readable. A {@code getX()} returning something unrelated is a different property, not a companion - but
     * {@code boolean getX()} beside {@code Boolean isX()} <em>is</em> one, and is one of the two shapes a developer
     * reaches for to repair an unpaired accessor, so demanding the wrapper exactly would have let this guard fail a
     * correct fix.
     */
    static boolean hasCompanionGetter(final Class<?> type, final Method accessor) {
        final String companionName = "get" + accessor.getName().substring(2);
        return Stream.of(type.getMethods())
                .anyMatch(method -> method.getParameterCount() == 0
                        && !Modifier.isStatic(method.getModifiers())
                        && companionName.equals(method.getName())
                        && isBooleanValued(method.getReturnType()));
    }

    private static boolean isBooleanValued(final Class<?> returnType) {
        return Boolean.class == returnType || boolean.class == returnType;
    }

}
