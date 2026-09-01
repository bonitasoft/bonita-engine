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
package org.bonitasoft.engine.business.data.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.bonitasoft.engine.bdm.serialization.BusinessDataObjectMapper;
import org.bonitasoft.engine.business.data.generator.server.ServerBDMCodeGenerator;
import org.bonitasoft.engine.business.data.generator.server.ServerBDMJarBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Pins the JSON payload of a BOOLEAN attribute now that BPA-833 generates it with both <code>isHungry()</code> and
 * a <code>getHungry()</code> alias.
 * <p>
 * One production path serializes generated entities <em>through their getters</em>. {@code ExecuteBDMQueryCommand},
 * and so every {@code BusinessObjectDAO} query result, goes through {@link BusinessDataObjectMapper}, which does not
 * restrict visibility to fields the way {@code JsonBusinessDataSerializerImpl} does. Jackson gives the get-prefixed
 * accessor precedence and derives the same property name from either, so the payload is unchanged. That is Jackson
 * behaviour rather than a documented guarantee, hence this test, and against the real
 * {@code BusinessDataObjectMapper} so that a visibility or naming-strategy change there is caught.
 * <p>
 * It lives in this module because it needs a generated entity, and only the generator depends on
 * {@code bonita-common}, not the reverse.
 */
public class GeneratedEntitySerializationTest {

    private static final String DINOSAUR_QUALIFIED_NAME = "com.company.Dinosaur";

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static URLClassLoader bdmClassLoader;

    private static Class<?> dinosaurClass;

    @BeforeClass
    public static void generateAndLoadBdm() throws Exception {
        final byte[] bdmJar = new ServerBDMJarBuilder(new ServerBDMCodeGenerator())
                .build(new BOMBuilder().buildModelWithSingleBoolean(), TrueFileFilter.TRUE);
        final File jarFile = temporaryFolder.newFile("server-bdm.jar");
        Files.write(jarFile.toPath(), bdmJar);
        bdmClassLoader = new URLClassLoader(new URL[] { jarFile.toURI().toURL() },
                GeneratedEntitySerializationTest.class.getClassLoader());
        dinosaurClass = bdmClassLoader.loadClass(DINOSAUR_QUALIFIED_NAME);
    }

    @AfterClass
    public static void closeBdmClassLoader() throws Exception {
        if (bdmClassLoader != null) {
            bdmClassLoader.close();
        }
    }

    @Test
    public void booleanAttribute_should_serialize_once_through_the_business_data_object_mapper() throws Exception {
        //given a generated Entity, which is what the DAO path serializes
        final Serializable dinosaur = (Serializable) dinosaurClass.getConstructor().newInstance();
        dinosaurClass.getMethod("setHungry", Boolean.class).invoke(dinosaur, Boolean.TRUE);

        //when
        final String json = new String(new BusinessDataObjectMapper().writeValueAsBytes(dinosaur));

        //then split rather than parse: parsing would silently collapse a duplicated property
        assertThat(json).contains("\"hungry\":true");
        assertThat(json.split("\"hungry\"", -1)).as("the two accessors must collapse to a single property")
                .hasSize(2);
    }

    /**
     * The other half of the same path: a {@code BusinessObjectDAO} result is serialized on the server and read back
     * into a generated entity on the client, through the same mapper. Pinned too, since a visibility or
     * naming-strategy change would break reading just as readily as writing.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void booleanAttribute_should_survive_a_round_trip_through_the_business_data_object_mapper()
            throws Exception {
        //given
        final BusinessDataObjectMapper mapper = new BusinessDataObjectMapper();
        final Serializable dinosaur = (Serializable) dinosaurClass.getConstructor().newInstance();
        dinosaurClass.getMethod("setHungry", Boolean.class).invoke(dinosaur, Boolean.TRUE);
        final byte[] json = mapper.writeValueAsBytes(dinosaur);

        //when
        final Serializable readBack = mapper.readValue(json, (Class<Serializable>) dinosaurClass);

        //then both accessors agree with what was written
        assertThat(dinosaurClass.getMethod("isHungry").invoke(readBack)).isEqualTo(Boolean.TRUE);
        assertThat(dinosaurClass.getMethod("getHungry").invoke(readBack)).isEqualTo(Boolean.TRUE);
    }

    /**
     * {@code readListValue} is the method {@code ExecuteBDMQueryCommand} results actually come back through, and it
     * configures its own {@code CollectionType}, so it is pinned separately from the single-value read above.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void booleanAttribute_should_survive_a_list_round_trip_through_the_business_data_object_mapper()
            throws Exception {
        //given
        final BusinessDataObjectMapper mapper = new BusinessDataObjectMapper();
        final Serializable dinosaur = (Serializable) dinosaurClass.getConstructor().newInstance();
        dinosaurClass.getMethod("setHungry", Boolean.class).invoke(dinosaur, Boolean.TRUE);
        final byte[] json = mapper.writeValueAsBytes(new ArrayList<>(List.of(dinosaur)));

        //when
        final List<Serializable> readBack = mapper.readListValue(json, (Class<Serializable>) dinosaurClass);

        //then
        assertThat(readBack).singleElement()
                .satisfies(entity -> assertThat(dinosaurClass.getMethod("isHungry").invoke(entity))
                        .isEqualTo(Boolean.TRUE));
    }

}
