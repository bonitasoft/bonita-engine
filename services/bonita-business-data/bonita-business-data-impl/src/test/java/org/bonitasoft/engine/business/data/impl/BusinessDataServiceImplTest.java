/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.business.data.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.QueryParameter;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.JsonBusinessDataSerializer;
import org.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.commons.TypeConverterUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonGenerationException;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataServiceImplTest {

    private static final String PARAMETER_BUSINESSDATA_CLASS_URI_VALUE = "/businessdata/{className}/{id}/{field}";
    private static final String NEW_NAME = "new name";
    public static final String PARAMETER_STRING = "parameterString";
    public static final String PARAMETER_INTEGER = "parameterInteger";
    public static final String PARAMETER_LONG = "parameterLong";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public class EntityPojo implements Entity {

        private static final long serialVersionUID = 1L;
        private String name;
        private Boolean bool;
        private Date date;
        private List<Long> numbers;

        @OneToOne(cascade = CascadeType.MERGE)
        private Entity aggregationEntity;

        @OneToOne(cascade = CascadeType.ALL)
        private Entity compositionEntity;

        @OneToOne(cascade = CascadeType.ALL)
        private Entity nullChildEntity;

        @OneToMany(cascade = CascadeType.MERGE)
        private List<Entity> aggregationEntities;

        @OneToMany(cascade = CascadeType.ALL)
        private List<Entity> compositionEntities;

        private final Long persistenceId;

        public EntityPojo(final Long persistenceId) {
            this.persistenceId = persistenceId;
            aggregationEntities = new ArrayList<Entity>();
            compositionEntities = new ArrayList<Entity>();

        }

        @Override
        public Long getPersistenceId() {
            return persistenceId;
        }

        @Override
        public Long getPersistenceVersion() {
            return 2L;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(final Date date) {
            this.date = date;
        }

        public Boolean getBool() {
            return bool;
        }

        public void setBool(final Boolean bool) {
            this.bool = bool;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public List<Long> getNumbers() {
            return numbers;
        }

        public void setNumbers(final List<Long> numbers) {
            this.numbers = numbers;
        }

        public Entity getCompositionEntity() {
            return compositionEntity;
        }

        public void setCompositionEntity(final Entity compositionEntity) {
            this.compositionEntity = compositionEntity;
        }

        public Entity getAggregationEntity() {
            return aggregationEntity;
        }

        public void setAggregationEntity(final Entity aggregationEntity) {
            this.aggregationEntity = aggregationEntity;
        }

        public Entity getNullChildEntity() {
            return null;
        }

        public void setNullChildEntity(final Entity nullChildEntity) {
            this.nullChildEntity = nullChildEntity;
        }

        public List<Entity> getAggregationEntities() {
            return aggregationEntities;
        }

        public void setAggregationEntities(final List<Entity> aggregationEntities) {
            this.aggregationEntities = aggregationEntities;
        }

        public List<Entity> getCompositionEntities() {
            return compositionEntities;
        }

        public void setCompositionEntities(final List<Entity> compositionEntities) {
            this.compositionEntities = compositionEntities;
        }
    }

    private final Entity pojo = new EntityPojo(1L);
    private BusinessDataServiceImpl businessDataService;

    @Mock
    private BusinessDataRepository businessDataRepository;

    @Mock
    private Entity businessData;

    @Mock
    JsonBusinessDataSerializer jsonEntitySerializer;

    @Mock
    BusinessDataModelRepository businessDataModelRepository;

    private TypeConverterUtil typeConverterUtil;

    @Before
    public void before() throws Exception {
        final String[] datePatterns = new String[] { "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd", "HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS" };
        typeConverterUtil = new TypeConverterUtil(datePatterns);
        businessDataService = spy(new BusinessDataServiceImpl(businessDataRepository, jsonEntitySerializer, businessDataModelRepository, typeConverterUtil));
    }

    @Test
    public void isBusinessDataShouldBeTrue() throws Exception {

        final Entity pojo = new EntityPojo(1L);
        assertThat(businessDataService.isBusinessData(pojo)).isTrue();
    }

    @Test
    public void isBusinessDataShouldBeTrueWithList() throws Exception {
        assertThat(businessDataService.isBusinessData(Arrays.asList(pojo))).isTrue();
        assertThat(businessDataService.isBusinessData(new ArrayList<String>())).isTrue();
    }

    @Test
    public void isBusinessDataShouldBeFalseWithList() throws Exception {
        assertThat(businessDataService.isBusinessData("not a list")).isFalse();
        assertThat(businessDataService.isBusinessData(Arrays.asList(new Long(1L)))).isFalse();
    }

    @Test
    public void isBusinessDataShouldBeFalse() throws Exception {
        final Object pojo = new Object();
        assertThat(businessDataService.isBusinessData(pojo)).isFalse();
    }

    @Test
    public void isBusinessDataShouldBeFalseWhenDataIsNull() throws Exception {
        assertThat(businessDataService.isBusinessData(null)).isFalse();

    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void callJavaOperationShouldThrowExceptionWhenBusinessDataIsNull() throws Exception {
        businessDataService.callJavaOperation(null, new EntityPojo(1L), "someMethod", String.class.getName());
    }

    @Test
    public void callJavaOperationShouldInvokeListMethod() throws Exception {
        final List<Entity> entities = new ArrayList<Entity>();
        entities.add(new EntityPojo(1L));
        businessDataService.callJavaOperation(entities, entities, "contains", Object.class.getName());
    }

    @Test(expected = SBusinessDataRepositoryException.class)
    public void callJavaOperationShouldThrowExceptionWhenNotAnEntity() throws Exception {
        businessDataService.callJavaOperation("not an entity", null, "getLengh", String.class.getName());
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void callJavaOperationShouldThrowExceptionWhenBusinessDataIsNotFound() throws Exception {
        //given
        doThrow(SBusinessDataNotFoundException.class).when(businessDataRepository).findById(pojo.getClass(), pojo.getPersistenceId());

        //when
        businessDataService.callJavaOperation(pojo, new EntityPojo(1L), "getName", String.class.getName());
    }

    @Test(expected = SBusinessDataRepositoryException.class)
    public void callJavaOperationShouldThrowExceptionWheninvokeFails() throws Exception {
        //given
        doThrow(SBusinessDataNotFoundException.class).when(businessDataService).invokeJavaMethod(pojo, "setName", String.class.getName(), "name");
        //when
        businessDataService.callJavaOperation(pojo, new EntityPojo(1L), "someMethod", String.class.getName());
    }

    @Test
    public void callJavaOperationShouldSetValue() throws Exception {
        //given
        doReturn(pojo).when(businessDataRepository).findById(pojo.getClass(), pojo.getPersistenceId());
        doReturn(pojo).when(businessDataRepository).merge(pojo);

        //when
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, NEW_NAME, "setName", String.class.getName());

        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getName()).as("should have set name").isEqualTo(NEW_NAME);
    }

    @Test
    public void callJavaOperationSetValueToNull() throws Exception {
        //given
        doReturn(pojo).when(businessDataRepository).findById(pojo.getClass(), pojo.getPersistenceId());
        doReturn(pojo).when(businessDataRepository).merge(pojo);

        //when
        businessDataService.callJavaOperation(pojo, NEW_NAME, "setName", String.class.getName());
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, null, "setName", String.class.getName());

        //then
        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getName()).as("should have set name to null").isNull();
    }

    @Test
    public void callJavaOperationShouldSetEntityComposition() throws Exception {
        //given
        final EntityPojo compositionEntity = new EntityPojo(2L);

        doReturn(pojo).when(businessDataRepository).findById(pojo.getClass(), pojo.getPersistenceId());
        doReturn(pojo).when(businessDataRepository).merge(pojo);

        //when
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, compositionEntity, "setCompositionEntity",
                Entity.class.getName());

        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getCompositionEntity()).as("should have set entity").isEqualTo(compositionEntity);
        verify(businessDataRepository, never()).findById(compositionEntity.getClass(), compositionEntity.getPersistenceId());
    }

    @Test
    public void callJavaOperationShouldSetEntityAggregation() throws Exception {
        //given
        final EntityPojo compositionEntity = new EntityPojo(2L);

        doReturn(pojo).when(businessDataRepository).findById(pojo.getClass(), pojo.getPersistenceId());
        doReturn(pojo).when(businessDataRepository).merge(pojo);

        //when
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, compositionEntity, "setCompositionEntity",
                Entity.class.getName());

        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getCompositionEntity()).as("should have set entity").isEqualTo(compositionEntity);
        verify(businessDataRepository, never()).findById(compositionEntity.getClass(), compositionEntity.getPersistenceId());
    }

    @Test
    public void callJavaOperationShouldLoadEntitiesIfAggregation() throws Exception {
        //given
        final Long persistenceId1 = 1562L;
        final Long persistenceId2 = 9658L;
        final EntityPojo entity1 = new EntityPojo(persistenceId1);
        final EntityPojo entity2 = new EntityPojo(persistenceId2);
        final List<EntityPojo> entities = Arrays.asList(entity1, entity2);
        final List<Long> keys = Arrays.asList(persistenceId1, persistenceId2);

        doReturn(pojo).when(businessDataRepository).findById(pojo.getClass(), pojo.getPersistenceId());
        doReturn(entities).when(businessDataRepository).findByIds(EntityPojo.class, keys);
        doReturn(pojo).when(businessDataRepository).merge(pojo);

        //when
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, entities, "setAggregationEntities", List.class.getName());

        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getAggregationEntities()).as("should have set entities").isEqualTo(entities);
        verify(businessDataRepository).findByIds(EntityPojo.class, keys);
    }

    @Test
    public void callJavaOperationShouldNotLoadEntitiesIfComposition() throws Exception {
        //given
        final Long persistenceId1 = 1562L;
        final Long persistenceId2 = 9658L;
        final EntityPojo entity1 = new EntityPojo(persistenceId1);
        final EntityPojo entity2 = new EntityPojo(persistenceId2);
        final List<EntityPojo> entities = Arrays.asList(entity1, entity2);
        final List<Long> keys = Arrays.asList(persistenceId1, persistenceId2);

        doReturn(pojo).when(businessDataRepository).findById(pojo.getClass(), pojo.getPersistenceId());
        //   doReturn(entities).when(businessDataRepository).findByIds(EntityPojo.class, keys);
        doReturn(pojo).when(businessDataRepository).merge(pojo);

        //when
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, entities, "setCompositionEntities", List.class.getName());

        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getCompositionEntities()).as("should have set entities").isEqualTo(entities);
        verify(businessDataRepository, never()).findByIds(EntityPojo.class, keys);
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void callJavaOperationShouldThrowExceptionWhenPersistenceIdIsNull() throws Exception {
        //given
        final Long persistenceId1 = 1562L;

        final EntityPojo entity1 = new EntityPojo(persistenceId1);
        final EntityPojo entity2 = new EntityPojo(null);
        final List<EntityPojo> entities = Arrays.asList(entity1, entity2);
        final List<Long> keys = Arrays.asList(persistenceId1, null);

        doReturn(pojo).when(businessDataRepository).findById(pojo.getClass(), pojo.getPersistenceId());
        doReturn(entities).when(businessDataRepository).findByIds(EntityPojo.class, keys);
        doReturn(pojo).when(businessDataRepository).merge(pojo);

        //when
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, entities, "setAggregationEntities", List.class.getName());

        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getAggregationEntities()).as("should have set entities").isEqualTo(entities);
    }

    @Test
    public void callJavaOperationWithEmptyList() throws Exception {
        //given
        final List<EntityPojo> entities = Arrays.asList();
        final List<Long> keys = Arrays.asList();

        doReturn(pojo).when(businessDataRepository).findById(pojo.getClass(), pojo.getPersistenceId());
        doReturn(entities).when(businessDataRepository).findByIds(EntityPojo.class, keys);
        doReturn(pojo).when(businessDataRepository).merge(pojo);

        //when
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, entities, "setAggregationEntities", List.class.getName());

        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getAggregationEntities()).as("should have set entities").isEmpty();
    }

    @Test
    public void callJavaOperationShouldSetListValue() throws Exception {
        //given
        final List<Long> longs = Arrays.asList(1L, 2L);
        doReturn(pojo).when(businessDataRepository).findById(pojo.getClass(), pojo.getPersistenceId());
        doReturn(pojo).when(businessDataRepository).merge(pojo);

        //when
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, longs, "setNumbers", List.class.getName());

        //then
        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getNumbers()).as("should have set list").hasSize(2).contains(1L, 2L);
    }

    @Test
    public void callJavaOperationShouldSetListToNull() throws Exception {
        //given
        final List<Long> longs = Arrays.asList(1L, 2L);
        doReturn(pojo).when(businessDataRepository).findById(pojo.getClass(), pojo.getPersistenceId());
        doReturn(pojo).when(businessDataRepository).merge(pojo);

        //when
        businessDataService.callJavaOperation(pojo, longs, "setNumbers", List.class.getName());
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, null, "setNumbers", List.class.getName());

        //then
        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getNumbers()).as("should have set list to null").isNull();
    }

    @Test
    public void shouldSetListOnBusinessDataReplaceTheList() throws Exception {
        //given
        final Long persistenceId2 = 9658L;
        final EntityPojo entity1 = new EntityPojo(1562L);
        final EntityPojo entity2 = new EntityPojo(persistenceId2);
        final EntityPojo entityPojo = new EntityPojo(1L);
        entityPojo.getAggregationEntities().add(entity1);

        final List<EntityPojo> newEntities = Arrays.asList(entity2);
        final List<Long> keys2 = Arrays.asList(persistenceId2);
        doReturn(entityPojo).when(businessDataRepository).findById(entityPojo.getClass(), entityPojo.getPersistenceId());
        doReturn(newEntities).when(businessDataRepository).findByIds(entity2.getClass(), keys2);
        doReturn(entityPojo).when(businessDataRepository).merge(entityPojo);

        //when
        final EntityPojo resultPojo = (EntityPojo) businessDataService.callJavaOperation(pojo, newEntities, "setAggregationEntities", List.class.getName());

        assertThat(resultPojo).as("should return object").isNotNull();
        assertThat(resultPojo.getAggregationEntities()).as("should have set entities").containsExactly(entity2);

    }

    @Test
    public void should_loadClass_find_the_class() throws Exception {
        //when
        final Class<? extends Entity> loadClass = businessDataService.loadClass(pojo.getClass().getName());

        //then
        assertThat(loadClass).isEqualTo(pojo.getClass());
    }

    @Test(expected = SBusinessDataRepositoryException.class)
    public void should_loadClass_throw_exception() throws Exception {
        //when
        businessDataService.loadClass("not a class");

        //then exception
    }

    @Test
    public void should_getJsonEntity_serialize_entity() throws Exception {
        //given
        doReturn(pojo).when(businessDataRepository).findById(pojo.getClass(), pojo.getPersistenceId());

        //when
        businessDataService.getJsonEntity(pojo.getClass().getName(), pojo.getPersistenceId(), PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        //then
        verify(jsonEntitySerializer).serializeEntity(pojo, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

    }

    @Test(expected = SBusinessDataRepositoryException.class)
    public void should_getJsonEntity_throw_exception() throws Exception {
        //given
        doReturn(pojo).when(businessDataRepository).findById(pojo.getClass(), pojo.getPersistenceId());
        doThrow(JsonGenerationException.class).when(jsonEntitySerializer).serializeEntity(pojo, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        //when then exception
        businessDataService.getJsonEntity(pojo.getClass().getName(), pojo.getPersistenceId(), PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);
    }

    @Test
    public void should_getJsonChildEntity_return_emptyObject() throws Exception {
        //given
        final EntityPojo parentEntity = new EntityPojo(1562L);
        final EntityPojo childEntity = new EntityPojo(156842L);
        parentEntity.setAggregationEntity(childEntity);

        doReturn(parentEntity).when(businessDataRepository).findById(parentEntity.getClass(), parentEntity.getPersistenceId());
        doReturn(childEntity).when(businessDataRepository).findById(childEntity.getClass(), childEntity.getPersistenceId());
        doReturn(childEntity).when(businessDataRepository).unwrap(childEntity);

        //when
        final Serializable jsonChildEntity = businessDataService.getJsonChildEntity(parentEntity.getClass().getName(), parentEntity.getPersistenceId(),
                "nullChildEntity",
                PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        //then
        assertThat(jsonChildEntity).isEqualTo(JsonBusinessDataSerializer.EMPTY_OBJECT);

    }

    @Test
    public void should_getJsonChildEntity_serialize_entity() throws Exception {
        //given
        final EntityPojo parentEntity = new EntityPojo(1562L);
        final EntityPojo childEntity = new EntityPojo(156842L);
        parentEntity.setAggregationEntity(childEntity);

        doReturn(parentEntity).when(businessDataRepository).findById(parentEntity.getClass(), parentEntity.getPersistenceId());
        doReturn(childEntity).when(businessDataRepository).findById(childEntity.getClass(), childEntity.getPersistenceId());
        doReturn(childEntity).when(businessDataRepository).unwrap(childEntity);

        //when
        businessDataService.getJsonChildEntity(parentEntity.getClass().getName(), parentEntity.getPersistenceId(), "aggregationEntity",
                PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        //then
        verify(jsonEntitySerializer).serializeEntity(childEntity, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

    }

    @Test
    public void should_getJsonChildEntity_serialize_entity_list() throws Exception {
        //given
        final EntityPojo parentEntity = new EntityPojo(1562L);
        final EntityPojo childEntity = new EntityPojo(156842L);
        parentEntity.getAggregationEntities().add(childEntity);

        doReturn(parentEntity).when(businessDataRepository).findById(parentEntity.getClass(), parentEntity.getPersistenceId());
        doReturn(childEntity).when(businessDataRepository).findById(childEntity.getClass(), childEntity.getPersistenceId());
        //   doReturn(childEntity).when(businessDataRepository).unwrap(childEntity);

        //when
        businessDataService.getJsonChildEntity(parentEntity.getClass().getName(), parentEntity.getPersistenceId(), "aggregationEntities",
                PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        //then
        final List<Entity> list = new ArrayList<Entity>();
        list.add(childEntity);
        verify(jsonEntitySerializer).serializeEntity(list, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

    }

    @Test
    public void getJsonQueryEntities_should_return_json() throws Exception {
        //given
        final EntityPojo entity = new EntityPojo(1562L);
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(PARAMETER_STRING, "a");
        parameters.put(PARAMETER_INTEGER, "12");
        parameters.put(PARAMETER_LONG, "34");

        doReturn(entity.getClass()).when(businessDataService).loadClass(entity.getClass().getName());

        final List<Entity> entities = new ArrayList<Entity>();
        entities.add(entity);
        doReturn(entities).when(businessDataRepository).findListByNamedQuery(anyString(), any(Class.class), anyMap(), anyInt(), anyInt());

        //given
        final BusinessObjectModel businessObjectModel = getBusinessObjectModel(entity);
        doReturn(businessObjectModel).when(businessDataModelRepository).getBusinessObjectModel();

        //when
        businessDataService.getJsonQueryEntities(entity.getClass().getName(), "query", parameters, 0, 10,
                PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        //then
        verify(jsonEntitySerializer).serializeEntity(entities, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);
    }

    @Test
    public void getJsonQueryEntities_should_throw_exception_when_query_not_found() throws Exception {
        expectedException.expect(SBusinessDataRepositoryException.class);
        expectedException.expectMessage("unable to get query wrongQuery for business object " + EntityPojo.class.getName());

        //given
        final EntityPojo entity = new EntityPojo(1562L);
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(PARAMETER_STRING, "a");
        parameters.put(PARAMETER_INTEGER, "12");
        parameters.put(PARAMETER_LONG, "34");

        doReturn(entity.getClass()).when(businessDataService).loadClass(entity.getClass().getName());

        final BusinessObjectModel businessObjectModel = getBusinessObjectModel(entity);
        doReturn(businessObjectModel).when(businessDataModelRepository).getBusinessObjectModel();

        //when then exception
        businessDataService.getJsonQueryEntities(entity.getClass().getName(), "wrongQuery", parameters, 0, 10,
                PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);
    }

    @Test
    public void getJsonQueryEntities_should_find_provided_query() throws Exception {

        //given
        final EntityPojo entity = new EntityPojo(1562L);
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(PARAMETER_STRING, "a");
        parameters.put(PARAMETER_INTEGER, "12");
        parameters.put(PARAMETER_LONG, "34");

        doReturn(entity.getClass()).when(businessDataService).loadClass(entity.getClass().getName());
        final BusinessObjectModel businessObjectModel = getBusinessObjectModel(entity);
        doReturn(businessObjectModel).when(businessDataModelRepository).getBusinessObjectModel();

        //when then no exception
        businessDataService.getJsonQueryEntities(entity.getClass().getName(), "find", parameters, 0, 10,
                PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);
    }

    @Test
    public void getJsonQueryEntities_should_check_parameters() throws Exception {
        expectedException.expect(SBusinessDataRepositoryException.class);
        expectedException.expectMessage("parameter(s) are missing for query named query :");
        expectedException.expectMessage(PARAMETER_INTEGER);
        expectedException.expectMessage(PARAMETER_STRING);
        expectedException.expectMessage(PARAMETER_LONG);

        //given
        final EntityPojo entity = new EntityPojo(1562L);
        doReturn(entity.getClass()).when(businessDataService).loadClass(entity.getClass().getName());

        final BusinessObjectModel businessObjectModel = getBusinessObjectModel(entity);
        doReturn(businessObjectModel).when(businessDataModelRepository).getBusinessObjectModel();

        //when then exception
        businessDataService.getJsonQueryEntities(entity.getClass().getName(), "query", null, 0, 10,
                PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);
    }

    private BusinessObjectModel getBusinessObjectModel(final EntityPojo entity) {
        BusinessObjectModel businessObjectModel;
        businessObjectModel = new BusinessObjectModel();

        final Query query = new Query("query", "content", String.class.getName());
        query.getQueryParameters().add(new QueryParameter(PARAMETER_STRING, String.class.getName()));
        query.getQueryParameters().add(new QueryParameter(PARAMETER_INTEGER, Integer.class.getName()));
        query.getQueryParameters().add(new QueryParameter(PARAMETER_LONG, Long.class.getName()));

        final BusinessObject businessObject = new BusinessObject();
        businessObject.setQualifiedName(entity.getClass().getName());
        businessObject.setQueries(Arrays.asList(query));
        businessObjectModel.getBusinessObjects().add(businessObject);

        return businessObjectModel;
    }

    @Test
    public void getJsonEntities_should_serialize_entities() throws Exception {
        final long identifier1 = 1983L;
        final long identifier2 = 1990L;
        final EntityPojo pojo1 = new EntityPojo(identifier1);
        final EntityPojo pojo2 = new EntityPojo(identifier2);
        final List<Long> identifiers = new ArrayList<>();
        identifiers.add(identifier1);
        identifiers.add(identifier2);
        final List<EntityPojo> pojos = new ArrayList<>();
        pojos.add(pojo1);
        pojos.add(pojo2);
        when(businessDataRepository.findByIds(EntityPojo.class, identifiers)).thenReturn(pojos);

        businessDataService.getJsonEntities(EntityPojo.class.getName(), identifiers, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        verify(jsonEntitySerializer).serializeEntity(pojos, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);
    }

    @Test(expected = SBusinessDataRepositoryException.class)
    public void getJsonEntities_should_throw_exception_if_the_serialization_fails() throws Exception {
        final long identifier1 = 1983L;
        final long identifier2 = 1990L;
        final EntityPojo pojo1 = new EntityPojo(identifier1);
        final EntityPojo pojo2 = new EntityPojo(identifier2);
        final List<Long> identifiers = new ArrayList<>();
        identifiers.add(identifier1);
        identifiers.add(identifier2);
        final List<EntityPojo> pojos = new ArrayList<>();
        pojos.add(pojo1);
        pojos.add(pojo2);
        when(businessDataRepository.findByIds(EntityPojo.class, identifiers)).thenReturn(pojos);
        when(jsonEntitySerializer.serializeEntity(pojos, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE)).thenThrow(new IOException("exception"));

        businessDataService.getJsonEntities(EntityPojo.class.getName(), identifiers, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);
    }

}
