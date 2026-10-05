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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * BPA-833: pins the accessor-pairing scan's own decisions.
 * <p>
 * The scan itself runs against a whole engine classpath, where these edge cases either do not occur yet or cannot be
 * arranged on demand. A guard nobody can test is a guard nobody can trust, so the three decisions that determine
 * whether it fires are pinned here against types declared for the purpose.
 */
class BooleanAccessorPairingScanTest {

    /** Declares one of each shape the scan has to tell apart. */
    @SuppressWarnings("unused")
    static class Shapes {

        public Boolean isPaired() {
            return Boolean.TRUE;
        }

        public Boolean getPaired() {
            return Boolean.TRUE;
        }

        public Boolean isPrimitivePaired() {
            return Boolean.TRUE;
        }

        /** A valid companion: the property is readable, so this must count as paired. */
        public boolean getPrimitivePaired() {
            return true;
        }

        public Boolean isUnpaired() {
            return Boolean.TRUE;
        }

        /** Not an accessor - no upper-case character after "is". */
        public Boolean isolate() {
            return Boolean.TRUE;
        }

        /** Not an accessor - JavaBeans accessors are instance methods. */
        public static Boolean isStaticFlag() {
            return Boolean.TRUE;
        }

        /** A companion of the wrong type is a different property, not a companion. */
        public Boolean isWronglyPaired() {
            return Boolean.TRUE;
        }

        public String getWronglyPaired() {
            return "";
        }
    }

    @Test
    void shouldFindOnlyInstanceWrapperBooleanIsAccessors() {
        //given
        final List<BooleanAccessorPairingScan.Uninspectable> uninspectable = new ArrayList<>();

        //when
        final List<Method> accessors = BooleanAccessorPairingScan.wrapperBooleanIsAccessorsOf(Shapes.class,
                uninspectable);

        //then a static Boolean isX() is not an accessor, so the scan must never demand a companion for it
        assertThat(accessors).extracting(Method::getName)
                .containsExactlyInAnyOrder("isPaired", "isPrimitivePaired", "isUnpaired", "isWronglyPaired")
                .doesNotContain("isStaticFlag", "isolate");
        assertThat(uninspectable).isEmpty();
    }

    @Test
    void shouldAcceptAPrimitiveCompanion() throws Exception {
        //given a Boolean isX() whose companion returns the primitive - a valid property getter, and one of the two
        //shapes a developer uses to repair an unpaired accessor
        final Method accessor = Shapes.class.getMethod("isPrimitivePaired");

        //when-then
        assertThat(BooleanAccessorPairingScan.hasCompanionGetter(Shapes.class, accessor)).isTrue();
    }

    @Test
    void shouldAcceptAWrapperCompanion() throws Exception {
        //given
        final Method accessor = Shapes.class.getMethod("isPaired");

        //when-then
        assertThat(BooleanAccessorPairingScan.hasCompanionGetter(Shapes.class, accessor)).isTrue();
    }

    @Test
    void shouldRejectACompanionOfAnUnrelatedType() throws Exception {
        //given a getX() returning String - a different property, not a companion
        final Method accessor = Shapes.class.getMethod("isWronglyPaired");

        //when-then
        assertThat(BooleanAccessorPairingScan.hasCompanionGetter(Shapes.class, accessor)).isFalse();
    }

    @Test
    void shouldReportAnUnpairedAccessorAsUnpaired() throws Exception {
        //given
        final Method accessor = Shapes.class.getMethod("isUnpaired");

        //when-then
        assertThat(BooleanAccessorPairingScan.hasCompanionGetter(Shapes.class, accessor)).isFalse();
    }

    @Test
    void shouldReadTheMissingTypeFromAClassNameShapedMessage() {
        //when-then the internal form is what NoClassDefFoundError carries
        assertThat(BooleanAccessorPairingScan
                .missingTypeOf(new NoClassDefFoundError("jakarta/servlet/http/HttpServlet")))
                .isEqualTo("jakarta.servlet.http.HttpServlet");
        assertThat(BooleanAccessorPairingScan
                .missingTypeOf(new ClassNotFoundException("com.sap.conn.jco.ext.DestinationDataProvider")))
                .isEqualTo("com.sap.conn.jco.ext.DestinationDataProvider");
        assertThat(BooleanAccessorPairingScan.missingTypeOf(new NoClassDefFoundError("org/bonitasoft/Nested$Inner")))
                .as("a nested type is still a class name")
                .isEqualTo("org.bonitasoft.Nested$Inner");
    }

    /**
     * Not every such message is a bare type name, and taking one whole gets the tolerance decision wrong in both
     * directions: {@code "org.bonitasoft.Bar (wrong name: bar)"} starts with {@code org.bonitasoft.} and would fail
     * the build naming the wrong cause, while {@code "Could not initialize class org.bonitasoft.X"} does not and
     * would tolerate a genuinely broken Bonita class. Both must yield null, which the scan's assertion reports as an
     * anomaly to investigate rather than tolerating.
     */
    @Test
    void shouldYieldNoMissingTypeWhenTheMessageIsNotJustAClassName() {
        //when-then
        assertThat(BooleanAccessorPairingScan
                .missingTypeOf(new NoClassDefFoundError("org/bonitasoft/Bar (wrong name: bar)"))).isNull();
        assertThat(BooleanAccessorPairingScan
                .missingTypeOf(new NoClassDefFoundError("Could not initialize class org.bonitasoft.X"))).isNull();
        assertThat(BooleanAccessorPairingScan.missingTypeOf(new NoClassDefFoundError(""))).isNull();
        assertThat(BooleanAccessorPairingScan.missingTypeOf(new NoClassDefFoundError((String) null))).isNull();
    }

    @Test
    void shouldYieldNoMissingTypeForAFailureThatIsNotAMissingClass() {
        //when-then a failure the scan has not accounted for must not be silently tolerated
        assertThat(BooleanAccessorPairingScan.missingTypeOf(new ExceptionInInitializerError())).isNull();
        assertThat(BooleanAccessorPairingScan.missingTypeOf(new IllegalStateException("org.bonitasoft.X"))).isNull();
    }

}
