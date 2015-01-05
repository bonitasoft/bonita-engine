package com.bonitasoft.engine.business.data.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.OneToOne;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.JsonBusinessDataSerializer;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import com.fasterxml.jackson.core.JsonGenerationException;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataServiceImplTest {

    private static final String PARAMETER_BUSINESSDATA_CLASS_URI_VALUE = "/businessdata/{className}/{id}/{field}";
    private static final String NEW_NAME = "new name";

    public class EntityPojo implements Entity {

        private String name;
        private Boolean bool;
        private Date date;
        private List<Long> numbers;

        @OneToOne(cascade = CascadeType.MERGE)
        private Entity aggregationEntity;

        @OneToOne(cascade = CascadeType.ALL)
        private Entity compositionEntity;

        private List<Entity> entities;
        private final Long persistenceId;

        public EntityPojo(final Long persistenceId) {
            this.persistenceId = persistenceId;
            entities = new ArrayList<Entity>();
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

        public List<Entity> getEntities() {
            return entities;
        }

        public void setEntities(final List<Entity> entities) {
            this.entities = entities;
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

    }

    private final Entity pojo = new EntityPojo(1L);
    private BusinessDataServiceImpl businessDataService;

    @Mock
    private BusinessDataRepository businessDataRepository;

    @Mock
    private Entity businessData;

    @Mock
    JsonBusinessDataSerializer jsonEntitySerializer;

    @Before
    public void before() throws Exception {
        businessDataService = spy(new BusinessDataServiceImpl(businessDataRepository, jsonEntitySerializer));
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
    public void callJavaOperationShouldThrhrowExceptionWhenBusinessDataIsNull() throws Exception {
        businessDataService.callJavaOperation(null, new EntityPojo(1L), "someMethod", String.class.getName());
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
    public void callJavaOperationShouldSetEntityAggregation() throws Exception {
        //given
        final EntityPojo aggragationEntity = new EntityPojo(2L);

        doReturn(pojo).when(businessDataRepository).findById(pojo.getClass(), pojo.getPersistenceId());
        doReturn(aggragationEntity).when(businessDataRepository).findById(aggragationEntity.getClass(), aggragationEntity.getPersistenceId());
        doReturn(pojo).when(businessDataRepository).merge(pojo);

        //when
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, aggragationEntity, "setAggregationEntity",
                Entity.class.getName());

        //then
        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getAggregationEntity()).as("should have set entity").isEqualTo(aggragationEntity);
        verify(businessDataRepository).findById(aggragationEntity.getClass(), aggragationEntity.getPersistenceId());
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
    public void callJavaOperationShouldLoadEntityIfAggregation() throws Exception {
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
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, entities, "setEntities", List.class.getName());

        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getEntities()).as("should have set entities").isEqualTo(entities);
        verify(businessDataRepository).findByIds(EntityPojo.class, keys);
    }

    @Test
    public void callJavaOperationShouldSetEntities() throws Exception {
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
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, entities, "setEntities", List.class.getName());

        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getEntities()).as("should have set entities").isEqualTo(entities);
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
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, entities, "setEntities", List.class.getName());

        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getEntities()).as("should have set entities").isEqualTo(entities);
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
        final EntityPojo pojoObject = (EntityPojo) businessDataService.callJavaOperation(pojo, entities, "setEntities", List.class.getName());

        assertThat(pojoObject).as("should return object").isNotNull();
        assertThat(pojoObject.getEntities()).as("should have set entities").isEmpty();
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
        entityPojo.getEntities().add(entity1);

        final List<EntityPojo> newEntities = Arrays.asList(entity2);
        final List<Long> keys2 = Arrays.asList(persistenceId2);
        doReturn(entityPojo).when(businessDataRepository).findById(entityPojo.getClass(), entityPojo.getPersistenceId());
        doReturn(newEntities).when(businessDataRepository).findByIds(entity2.getClass(), keys2);
        doReturn(entityPojo).when(businessDataRepository).merge(entityPojo);

        //when
        final EntityPojo resultPojo = (EntityPojo) businessDataService.callJavaOperation(pojo, newEntities, "setEntities", List.class.getName());

        assertThat(resultPojo).as("should return object").isNotNull();
        assertThat(resultPojo.getEntities()).as("should have set entities").containsExactly(entity2);

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
        final Class<? extends Entity> loadClass = businessDataService.loadClass("not a class");

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
        parentEntity.getEntities().add(childEntity);

        doReturn(parentEntity).when(businessDataRepository).findById(parentEntity.getClass(), parentEntity.getPersistenceId());
        doReturn(childEntity).when(businessDataRepository).findById(childEntity.getClass(), childEntity.getPersistenceId());
        //   doReturn(childEntity).when(businessDataRepository).unwrap(childEntity);

        //when
        businessDataService.getJsonChildEntity(parentEntity.getClass().getName(), parentEntity.getPersistenceId(), "entities",
                PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        //then
        final List<Entity> list = new ArrayList<Entity>();
        list.add(childEntity);
        verify(jsonEntitySerializer).serializeEntity(list, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

    }

}
