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
package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.commons.Pair.pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.bonitasoft.engine.data.instance.model.SBlobDataInstance;
import org.bonitasoft.engine.data.instance.model.SBooleanDataInstance;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.SDateDataInstance;
import org.bonitasoft.engine.data.instance.model.SDoubleDataInstance;
import org.bonitasoft.engine.data.instance.model.SFloatDataInstance;
import org.bonitasoft.engine.data.instance.model.SIntegerDataInstance;
import org.bonitasoft.engine.data.instance.model.SLongDataInstance;
import org.bonitasoft.engine.data.instance.model.SLongTextDataInstance;
import org.bonitasoft.engine.data.instance.model.SShortTextDataInstance;
import org.bonitasoft.engine.data.instance.model.SXMLDataInstance;
import org.bonitasoft.engine.data.instance.model.SXMLObjectDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SABlobDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SABooleanDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADateDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADoubleDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SAFloatDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SAIntegerDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SALongDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SALongTextDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SAShortTextDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SAXMLObjectDataInstance;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.test.persistence.repository.SADataInstanceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class SADataInstanceQueriesTest {

    private static final long CONTAINER_ID = 12345L;
    @Inject
    private SADataInstanceRepository repository;
    @Inject
    private JdbcTemplate jdbcTemplate;

    private void addSADataInstance(final int id, final String name, final int containerId, final String value,
            final long archiveDate, final long sourceObjectId) {
        repository.add(SAShortTextDataInstance.builder()
                .id(id)
                .name(name)
                .className(String.class.getName())
                .containerId(containerId)
                .containerType("PROCESS_INSTANCE")
                .value(value)
                .archiveDate(archiveDate)
                .sourceObjectId(sourceObjectId).build());
    }

    private SDataInstance.SDataInstanceBuilder<?, ?> fillCommonValues(
            SDataInstance.SDataInstanceBuilder<?, ?> builder) {
        return builder.className("theClassName")
                .containerId(CONTAINER_ID)
                .containerType("ContainerType")
                .description("My data");
    }

    private SADataInstance.SADataInstanceBuilder<?, ?> fillCommonValues(
            SADataInstance.SADataInstanceBuilder<?, ?> builder) {
        return builder.className("theClassName")
                .containerId(CONTAINER_ID)
                .containerType("ContainerType")
                .description("My data");
    }

    protected Map<String, Object> getDataUsingJDBC(SDataInstance dataInstance) {
        return jdbcTemplate.queryForMap("SELECT * FROM data_instance where id = " + dataInstance.getId());
    }

    protected Map<String, Object> getDataUsingJDBC(SADataInstance dataInstance) {
        return jdbcTemplate.queryForMap("SELECT * FROM arch_data_instance where id = " + dataInstance.getId());
    }

    protected PersistentObject getDataInstance(String dataName) {
        return repository.selectOne("getDataInstancesByNameAndContainer", pair("name", dataName),
                pair("containerId", CONTAINER_ID), pair("containerType", "ContainerType"));
    }

    protected PersistentObject getSADataInstance(String dataName) {
        return repository.selectOne("getLastSADataInstanceByContainer", pair("dataName", dataName),
                pair("containerId", CONTAINER_ID), pair("containerType", "ContainerType"));
    }

    @Test
    public void should_save_and_get_SXMLDataInstance() {
        SDataInstance dataInstance = repository.add(fillCommonValues(SXMLDataInstance.builder()
                .name("myXmlData")
                .element("theElement")
                .namespace("theNameSpace")
                .value("<xml></xml>")).build());

        PersistentObject persistentObject = getDataInstance("myXmlData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SXMLDataInstance.class);
        assertThat(dataAsMap).containsEntry("CLOBVALUE", "<xml></xml>");
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SXMLDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SShortTextDataInstance() {
        SDataInstance dataInstance = repository.add(fillCommonValues(SShortTextDataInstance.builder()
                .name("myTextData")
                .value("shortText")).build());

        PersistentObject persistentObject = getDataInstance("myTextData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SShortTextDataInstance.class);
        assertThat(dataAsMap).containsEntry("SHORTTEXTVALUE", "shortText");
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SShortTextDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SSShortTextDataInstance() {
        SADataInstance dataInstance = repository.add(fillCommonValues(SAShortTextDataInstance.builder()
                .name("myTextData")
                .value("shortText")).build());

        PersistentObject persistentObject = getSADataInstance("myTextData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SAShortTextDataInstance.class);
        assertThat(dataAsMap).containsEntry("SHORTTEXTVALUE", "shortText");
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SAShortTextDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SLongDataInstance() {
        SDataInstance dataInstance = repository.add(fillCommonValues(SLongDataInstance.builder()
                .name("myLongData")
                .value(1234567890L)).build());

        PersistentObject persistentObject = getDataInstance("myLongData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SLongDataInstance.class);
        assertThat(dataAsMap).containsEntry("LONGVALUE", 1234567890L);
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SLongDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SDoubleDataInstance() {
        SDataInstance dataInstance = repository.add(fillCommonValues(SDoubleDataInstance.builder()
                .name("myDoubleData")
                .value(1234567890.0)).build());

        PersistentObject persistentObject = getDataInstance("myDoubleData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SDoubleDataInstance.class);
        assertThat(dataAsMap).containsEntry("DOUBLEVALUE", 1234567890.0);
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SDoubleDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SXMLObjectDataInstance() {
        SDataInstance dataInstance = repository.add(fillCommonValues(SXMLObjectDataInstance.builder()
                .name("myXmlData")
                .value("<string>MyString<string>")).build());

        PersistentObject persistentObject = getDataInstance("myXmlData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SXMLObjectDataInstance.class);
        assertThat(dataAsMap).containsEntry("CLOBVALUE", "<string>MyString<string>");
        assertThat(((SXMLObjectDataInstance) persistentObject).getValue()).isEqualTo("MyString");
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SXMLObjectDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SIntegerDataInstance() {
        SDataInstance dataInstance = repository.add(fillCommonValues(SIntegerDataInstance.builder()
                .name("myIntData")
                .value(1234567890)).build());

        PersistentObject persistentObject = getDataInstance("myIntData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SIntegerDataInstance.class);
        assertThat(dataAsMap).containsEntry("INTVALUE", 1234567890);
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SIntegerDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SDateDataInstance() {
        Date date = new Date();
        SDataInstance dataInstance = repository.add(fillCommonValues(SDateDataInstance.builder()
                .name("myDateData")
                .value(date)).build());

        PersistentObject persistentObject = getDataInstance("myDateData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SDateDataInstance.class);
        assertThat(dataAsMap).containsEntry("LongValue", date.getTime());
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SDateDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SLongTextDataInstance() {
        Date date = new Date();
        SDataInstance dataInstance = repository.add(fillCommonValues(SLongTextDataInstance.builder()
                .name("myTextData")
                .value("lonnnnnnnnng text")).build());

        PersistentObject persistentObject = getDataInstance("myTextData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SLongTextDataInstance.class);
        assertThat(dataAsMap).containsEntry("CLOBVALUE", "lonnnnnnnnng text");
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SLongTextDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SFloatDataInstance() {
        SDataInstance dataInstance = repository.add(fillCommonValues(SFloatDataInstance.builder()
                .name("myFloatData")
                .value(1f)).build());

        PersistentObject persistentObject = getDataInstance("myFloatData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SFloatDataInstance.class);
        assertThat(dataAsMap.get("FLOATVALUE")).isEqualTo(1.0);
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SFloatDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SBlobDataInstance() {
        SDataInstance dataInstance = repository.add(fillCommonValues(SBlobDataInstance.builder()
                .name("myBlob")
                .value(new byte[] { 1, 2, 3 })).build());

        PersistentObject persistentObject = getDataInstance("myBlob");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SBlobDataInstance.class);
        assertThat(dataAsMap.get("BLOBVALUE")).isEqualTo(new byte[] { 1, 2, 3 });
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SBlobDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SBooleanDataInstance() {
        SDataInstance dataInstance = repository.add(fillCommonValues(SBooleanDataInstance.builder()
                .name("myBoolean")
                .value(true)).build());

        PersistentObject persistentObject = getDataInstance("myBoolean");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SBooleanDataInstance.class);
        assertThat(dataAsMap.get("BOOLEANVALUE")).isEqualTo(true);
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SBooleanDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SALongDataInstance() {
        SADataInstance dataInstance = repository.add(fillCommonValues(SALongDataInstance.builder()
                .name("myLongData")
                .value(1234567890L)).build());

        PersistentObject persistentObject = getSADataInstance("myLongData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SALongDataInstance.class);
        assertThat(dataAsMap).containsEntry("LONGVALUE", 1234567890L);
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SALongDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SADoubleDataInstance() {
        SADataInstance dataInstance = repository.add(fillCommonValues(SADoubleDataInstance.builder()
                .name("myDoubleData")
                .value(1234567890.0)).build());

        PersistentObject persistentObject = getSADataInstance("myDoubleData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SADoubleDataInstance.class);
        assertThat(dataAsMap).containsEntry("DOUBLEVALUE", 1234567890.0);
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SADoubleDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SAXMLObjectDataInstance() {
        SADataInstance dataInstance = repository.add(fillCommonValues(SAXMLObjectDataInstance.builder()
                .name("myXmlData")
                .value("<string>MyString<string>")).build());

        PersistentObject persistentObject = getSADataInstance("myXmlData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SAXMLObjectDataInstance.class);
        assertThat(dataAsMap).containsEntry("CLOBVALUE", "<string>MyString<string>");
        assertThat(((SAXMLObjectDataInstance) persistentObject).getValue()).isEqualTo("MyString");
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SAXMLObjectDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SAIntegerDataInstance() {
        SADataInstance dataInstance = repository.add(fillCommonValues(SAIntegerDataInstance.builder()
                .name("myIntData")
                .value(1234567890)).build());

        PersistentObject persistentObject = getSADataInstance("myIntData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SAIntegerDataInstance.class);
        assertThat(dataAsMap).containsEntry("INTVALUE", 1234567890);
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SAIntegerDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SADateDataInstance() {
        Date date = new Date();
        SADataInstance dataInstance = repository.add(fillCommonValues(SADateDataInstance.builder()
                .name("myDateData")
                .value(date)).build());

        PersistentObject persistentObject = getSADataInstance("myDateData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SADateDataInstance.class);
        assertThat(dataAsMap).containsEntry("LongValue", date.getTime());
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SADateDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SALongTextDataInstance() {
        SADataInstance dataInstance = repository.add(fillCommonValues(SALongTextDataInstance.builder()
                .name("myTextData")
                .value("lonnnnnnnnng text")).build());

        PersistentObject persistentObject = getSADataInstance("myTextData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SALongTextDataInstance.class);
        assertThat(dataAsMap).containsEntry("CLOBVALUE", "lonnnnnnnnng text");
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SALongTextDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SAFloatDataInstance() {
        SADataInstance dataInstance = repository.add(fillCommonValues(SAFloatDataInstance.builder()
                .name("myFloatData")
                .value(1f)).build());

        PersistentObject persistentObject = getSADataInstance("myFloatData");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SAFloatDataInstance.class);
        assertThat(dataAsMap.get("FLOATVALUE")).isEqualTo(1.0);
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SAFloatDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SABlobDataInstance() {
        SADataInstance dataInstance = repository.add(fillCommonValues(SABlobDataInstance.builder()
                .name("myBlob")
                .value(new byte[] { 1, 2, 3 })).build());

        PersistentObject persistentObject = getSADataInstance("myBlob");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SABlobDataInstance.class);
        assertThat(dataAsMap.get("BLOBVALUE")).isEqualTo(new byte[] { 1, 2, 3 });
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SABlobDataInstanceImpl");
    }

    @Test
    public void should_save_and_get_SABooleanDataInstance() {
        SADataInstance dataInstance = repository.add(fillCommonValues(SABooleanDataInstance.builder()
                .name("myBoolean")
                .value(true)).build());

        PersistentObject persistentObject = getSADataInstance("myBoolean");
        Map<String, Object> dataAsMap = getDataUsingJDBC(dataInstance);

        assertThat(persistentObject).isEqualTo(dataInstance);
        assertThat(persistentObject).isInstanceOf(SABooleanDataInstance.class);
        assertThat(dataAsMap.get("BOOLEANVALUE")).isEqualTo(true);
        assertThat(dataAsMap).containsEntry("DISCRIMINANT", "SABooleanDataInstanceImpl");
    }

    @Test
    public void getSADataInstancesByDataInstanceIdAndArchiveDate_should_return_an_empty_list_if_not_data_defined() {
        final List<Long> dataInstanceIds = new ArrayList<>();
        dataInstanceIds.add(4L);
        final long time = System.currentTimeMillis();

        final List<SADataInstance> dataInstances = repository
                .getSADataInstancesByDataInstanceIdAndArchiveDate(dataInstanceIds, time);

        assertThat(dataInstances).isEmpty();
    }

    @Test
    public void getSADataInstancesByDataInstanceIdAndArchiveDate_should_return_an_empty_list_if_not_matching_identifiers() {
        addSADataInstance(1025244, "identifiant", 41008, null, 1411051738348L, 205093L);
        final List<Long> dataInstanceIds = new ArrayList<>();
        dataInstanceIds.add(4L);

        final List<SADataInstance> dataInstances = repository
                .getSADataInstancesByDataInstanceIdAndArchiveDate(dataInstanceIds, 1411051738348L);

        assertThat(dataInstances).isEmpty();
    }

    @Test
    public void getSADataInstancesByDataInstanceIdAndArchiveDate_should_return_an_empty_list_if_not_matching_time() {
        final long archiveDate = 1411051738348L;
        addSADataInstance(1025244, "identifiant", 41008, null, archiveDate, 205093L);
        addSADataInstance(1025259, "identifiant", 41008, "matti", archiveDate + 1500, 205093L);

        final List<Long> dataInstanceIds = new ArrayList<>();
        dataInstanceIds.add(205093L);

        final long time = 0L;
        final List<SADataInstance> dataInstances = repository
                .getSADataInstancesByDataInstanceIdAndArchiveDate(dataInstanceIds, time);
        assertThat(dataInstances).isEmpty();
    }

    @Test
    public void getSADataInstancesByDataInstanceIdAndArchiveDate_should_get_the_latest_upate_of_all_data_updates() {
        final long archiveDate = 1411051738348L;
        addSADataInstance(1025244, "identifiant", 41008, null, archiveDate, 205093L);
        addSADataInstance(1025259, "identifiant", 41008, "matti", archiveDate + 1500, 205093L);
        addSADataInstance(1025356, "identifiant", 41008, "hannu", archiveDate + 2500, 205093L);

        final List<Long> dataInstanceIds = new ArrayList<>();
        dataInstanceIds.add(205093L);

        final List<SADataInstance> dataInstances = repository
                .getSADataInstancesByDataInstanceIdAndArchiveDate(dataInstanceIds, archiveDate + 5000);
        assertThat(dataInstances).hasSize(1);
        assertThat(dataInstances.get(0).getId()).isEqualTo(1025356);
    }

    @Test
    public void getSADataInstancesByDataInstanceIdAndArchiveDate_should_get_the_latest_upate_of_a_frame_of_data_updates() {
        final long archiveDate = 1411051738348L;
        addSADataInstance(1025244, "identifiant", 41008, null, archiveDate, 205093L);
        addSADataInstance(1025259, "identifiant", 41008, "matti", archiveDate + 1500, 205093L);
        addSADataInstance(1025356, "identifiant", 41008, "hannu", archiveDate + 2500, 205093L);

        final List<Long> dataInstanceIds = new ArrayList<>();
        dataInstanceIds.add(205093L);

        final List<SADataInstance> dataInstances = repository
                .getSADataInstancesByDataInstanceIdAndArchiveDate(dataInstanceIds, archiveDate + 2000);
        assertThat(dataInstances).hasSize(1);
        assertThat(dataInstances.get(0).getId()).isEqualTo(1025259);
    }

}
