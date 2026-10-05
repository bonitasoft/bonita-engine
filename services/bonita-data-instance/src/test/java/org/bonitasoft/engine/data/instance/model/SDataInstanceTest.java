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
package org.bonitasoft.engine.data.instance.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * BPA-833: <code>transientData</code> must be readable as a JavaBean property, because that is what Groovy resolves
 * the <code>dataInstance.transientData</code> shorthand against. The JavaBeans specification sanctions the
 * <code>is</code> accessor only for a <em>primitive</em> <code>boolean</code>, and Groovy 4 enforces it, so a
 * wrapper-<code>Boolean</code> <code>isTransientData()</code> would not be a property getter and the shorthand would
 * fall back to reading the private field - which fails on a subclass receiver, and every concrete data instance is a
 * subclass.
 * <p>
 * The backing field is a primitive and no implementation can return <code>null</code>, so the accessor is left to
 * Lombok's <code>@Data</code>, which yields primitive <code>boolean isTransientData()</code>.
 */
class SDataInstanceTest {

    @Test
    void transientData_should_be_a_readable_bean_property() throws Exception {
        //given a concrete data instance, the type a script is actually handed
        final Class<?> concreteDataInstance = SShortTextDataInstance.class;

        //when
        final Optional<PropertyDescriptor> transientData = Arrays
                .stream(Introspector.getBeanInfo(concreteDataInstance).getPropertyDescriptors())
                .filter(descriptor -> "transientData".equals(descriptor.getName()))
                .findFirst();

        //then
        assertThat(transientData).isPresent();
        assertThat(transientData.get().getReadMethod()).isNotNull();
        // the return type IS pinned here, unlike for the public DataInstance / MaintenanceDetails, whose contract is
        // a wrapper and whose readability is enforced by BooleanAccessorPairingTest instead: for this internal type
        // the primitive is the fix, so a silent revert to the wrapper has to fail
        assertThat(transientData.get().getReadMethod().getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    void isTransientData_should_return_the_configured_value() {
        //given
        final SShortTextDataInstance dataInstance = new SShortTextDataInstance();
        dataInstance.setTransientData(true);

        //when-then
        assertThat(dataInstance.isTransientData()).isTrue();
    }

    /**
     * BPA-833: {@code SDataInstance} is stored through {@code CacheService}, a Hazelcast distributed map in cluster
     * mode, so its UID has to stay at the value it had before the wrapper accessor was removed. See the constant's
     * javadoc for the {@code serialver} invocation both values came from.
     * <p>
     * What this catches is a UID that stops being the pinned one - a typo, or the field being deleted, after which
     * {@link ObjectStreamClass} reports the computed default instead. It cannot tell whether the pinned value is the
     * historically correct one: that is what measuring it established, and re-measuring means rebuilding the
     * merge-base. So treat a change to the literal below as requiring that measurement again, not as a value to
     * update until the test passes.
     * <p>
     * One gap is deliberate: {@code SDataDefinitionImpl} needed no pin because it already declares
     * {@code serialVersionUID = 1L}. The concrete subclasses are covered behaviourally by
     * {@link #concreteDataInstance_should_survive_a_java_serialization_round_trip()} rather than by pinning 12 more
     * constants - a subclass UID is computed from its own members, so this change moved none of them, and hand-deriving
     * a dozen values is how the wrong pin got here in the first place.
     */
    @Test
    void serialVersionUID_should_stay_stable_across_accessor_changes() {
        //when-then
        assertThat(ObjectStreamClass.lookup(SDataInstance.class).getSerialVersionUID())
                .as("the UID the serialization runtime will use for the abstract base - measured on the merge-base"
                        + " build, not chosen")
                .isEqualTo(-2379027971578351313L);
    }

    /**
     * The pinned UID protects the base class, but a concrete subclass is what actually goes into the
     * {@code CacheService} map, and nothing else asserts that one is serializable at all.
     * <p>
     * What this catches, measured rather than assumed: a subclass gaining a field that is not serializable fails here
     * with {@code NotSerializableException} - verified by injecting a {@code Thread} field. What it does
     * <strong>not</strong> catch is a change to the UID: writer and reader are the same class in the same JVM, so the
     * round trip is self-consistent whatever the UID happens to be - moving it and the assertion above in step leaves
     * this test green. Cross-version compatibility is the other test's job, and only the measured value makes that
     * one meaningful.
     */
    @Test
    void concreteDataInstance_should_survive_a_java_serialization_round_trip() throws Exception {
        //given a populated concrete instance, including the field whose accessor started BPA-833
        final SShortTextDataInstance original = new SShortTextDataInstance();
        original.setId(4815162342L);
        original.setName("aTransientData");
        original.setDescription("a description");
        original.setTransientData(true);
        original.setContainerId(1234L);
        original.setContainerType("PROCESS_INSTANCE");
        original.setValue("aValue");

        //when
        final byte[] serialized;
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
            out.flush();
            serialized = bytes.toByteArray();
        }
        final Object readBack;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serialized))) {
            readBack = in.readObject();
        }

        //then
        assertThat(readBack).isInstanceOf(SShortTextDataInstance.class).isEqualTo(original);
        assertThat(((SShortTextDataInstance) readBack).isTransientData())
                .as("the attribute whose accessor this change altered")
                .isTrue();
    }

}
