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
package org.bonitasoft.engine.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.persistence.model.Car;
import org.bonitasoft.engine.persistence.model.Child;
import org.bonitasoft.engine.persistence.model.Human;
import org.bonitasoft.engine.persistence.model.Parent;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.BatchInsertRecord;
import org.bonitasoft.engine.recorder.model.DeleteAllRecord;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TenantTest extends CommonBPMServicesTest {

    private ReadPersistenceService persistenceService;
    private Recorder recorder;


    @After
    public void after() throws SBonitaException {
        if (!getTransactionService().isTransactionActive()) {
            getTransactionService().begin();
        }
        recorder.recordDeleteAll(new DeleteAllRecord(Human.class, Collections.<FilterOption>emptyList()));
        getTransactionService().complete();
    }

    @Before
    public void before() throws SBonitaException {
        persistenceService = getTenantAccessor().getReadPersistenceService();
        recorder = getTenantAccessor().getRecorder();
    }

    @Test
    public void simpleStorage() throws Exception {
        getTransactionService().begin();

        final Human human = PersistenceTestUtil.buildHuman("parent1FN", "parent1LN", 45);
        recorder.recordInsert(new InsertRecord(human), null);
        PersistenceTestUtil.checkHuman(human, persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human.getId())));
        recorder.recordDelete(new DeleteRecord(human), null);

        getTransactionService().complete();
    }

    @Test
    public void selectById() throws Exception {
        getTransactionService().begin();

        final Human human = PersistenceTestUtil.buildHuman("parent1FN", "parent1LN", 45);
        recorder.recordInsert(new InsertRecord(human), null);
        assertNotNull(human.getId());
        assertTrue(human.getId() > 0);
        PersistenceTestUtil.checkHuman(human, persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human.getId())));
        recorder.recordDelete(new DeleteRecord(human), null);

        getTransactionService().complete();
    }

    @Test
    public void sequence() throws Exception {

        // Sequence id must be reinitialized.
        long myTenant = createTenant("myTenant");
        changeTenant(myTenant);

        getTransactionService().begin();

        final Human human1 = PersistenceTestUtil.buildHuman("h1fn", "h1ln", 45);
        final Human human2 = PersistenceTestUtil.buildHuman("h2fn", "h2ln", 45);
        final Human human3 = PersistenceTestUtil.buildHuman("h3fn", "h3ln", 45);
        final Car car1 = buildCar("brand1");
        final Car car2 = buildCar("brand2");
        final Car car3 = buildCar("brand3");

        recorder.recordInsert(new InsertRecord(human1), null);
        recorder.recordInsert(new InsertRecord(human2), null);
        recorder.recordInsert(new InsertRecord(car1), null);
        recorder.recordInsert(new InsertRecord(human3), null);
        recorder.recordInsert(new InsertRecord(car2), null);
        recorder.recordInsert(new InsertRecord(car3), null);

        assertEquals(1, human1.getId());
        assertEquals(2, human2.getId());
        assertEquals(3, human3.getId());
        assertEquals(1, car1.getId());
        assertEquals(2, car2.getId());
        assertEquals(3, car3.getId());

        recorder.recordDelete(new DeleteRecord(human1), null);
        recorder.recordDelete(new DeleteRecord(human2), null);
        recorder.recordDelete(new DeleteRecord(human3), null);
        recorder.recordDelete(new DeleteRecord(car1), null);
        recorder.recordDelete(new DeleteRecord(car2), null);
        recorder.recordDelete(new DeleteRecord(car3), null);

        getTransactionService().complete();
    }

    @Test
    public void sequenceLimit() throws Exception {
        getTransactionService().begin();

        final int numberOfInserts = 105;
        for (int i = 0; i < numberOfInserts; i++) {
            final Human human = PersistenceTestUtil.buildHuman("h1fn", "h1ln", 45);
            recorder.recordInsert(new InsertRecord(human), null);
        }
        getTransactionService().complete();
        getTransactionService().begin();
        assertEquals(Long.valueOf(numberOfInserts),
                persistenceService.selectOne(new SelectOneDescriptor<Long>("getNumberOfHumans", null, Human.class, Long.class)));
        getTransactionService().complete();
    }

    @Test
    public void deleteEntityFromObject() throws Exception {
        getTransactionService().begin();

        final Human human = PersistenceTestUtil.buildHuman("parent1FN", "parent1LN", 45);
        recorder.recordInsert(new InsertRecord(human), null);

        assertNotNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human.getId())));
        recorder.recordDelete(new DeleteRecord(human), null);
        assertNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human.getId())));

        getTransactionService().complete();
    }

    @Test
    public void deleteEntityFromIdInSameTransaction() throws Exception {
        getTransactionService().begin();

        final Human human = PersistenceTestUtil.buildHuman("parent1FN", "parent1LN", 45);
        recorder.recordInsert(new InsertRecord(human), null);

        assertNotNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human.getId())));
        recorder.recordDelete(new DeleteRecord(human), null);
        assertNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human.getId())));

        getTransactionService().complete();
    }

    @Test
    public void deleteEntitiesByTenantIdInSeparateTransaction() throws Exception {
        // First create the entities in the DB
        getTransactionService().begin();
        final Human human1 = PersistenceTestUtil.buildHuman("parent1FN", "parent1LN", 45);
        recorder.recordInsert(new InsertRecord(human1), null);
        final Human human2 = PersistenceTestUtil.buildHuman("parent2FN", "parent2LN", 54);
        recorder.recordInsert(new InsertRecord(human2), null);
        final Human human3 = PersistenceTestUtil.buildHuman("parent3FN", "parent3LN", 459);
        recorder.recordInsert(new InsertRecord(human3), null);
        getTransactionService().complete();

        // Ensure in a separate transaction that they are correctly inserted
        getTransactionService().begin();
        assertNotNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human1.getId())));
        assertNotNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human2.getId())));
        assertNotNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human3.getId())));
        getTransactionService().complete();

        // Delete them all.
        getTransactionService().begin();
        recorder.recordDeleteAll(new DeleteAllRecord(Human.class, Collections.<FilterOption>emptyList()));
        getTransactionService().complete();

        // Ensure in a separate transaction that they are correctly deleted
        getTransactionService().begin();
        assertNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human1.getId())));
        assertNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human2.getId())));
        assertNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human3.getId())));
        getTransactionService().complete();
    }

    @Test
    public void deleteAllInSameTransaction() throws Exception {
        // First create the entities in the DB
        getTransactionService().begin();
        final Human human1 = PersistenceTestUtil.buildHuman("parent1FN", "parent1LN", 45);
        final Human human2 = PersistenceTestUtil.buildHuman("parent2FN", "parent2LN", 54);
        recorder.recordInsert(new InsertRecord(human1), null);
        recorder.recordInsert(new InsertRecord(human2), null);

        assertNotNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human1.getId())));
        assertNotNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human2.getId())));

        // persistenceService.deleteAll(Human.class);

        recorder.recordDelete(new DeleteRecord(human1), null);
        recorder.recordDelete(new DeleteRecord(human2), null);

        assertNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human1.getId())));
        assertNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human2.getId())));
        getTransactionService().complete();
    }

    @Test
    public void deleteAllInSeparateTransaction() throws Exception {
        // First create the entities in the DB
        getTransactionService().begin();
        final Human human1 = PersistenceTestUtil.buildHuman("parent1FN", "parent1LN", 45);
        final Human human2 = PersistenceTestUtil.buildHuman("parent2FN", "parent2LN", 54);
        recorder.recordInsert(new InsertRecord(human1), null);
        recorder.recordInsert(new InsertRecord(human2), null);
        getTransactionService().complete();

        // Ensure in a separate transaction that they are correctly inserted
        getTransactionService().begin();
        assertNotNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human1.getId())));
        assertNotNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human2.getId())));
        getTransactionService().complete();

        // Delete them all.
        getTransactionService().begin();
        recorder.recordDeleteAll(new DeleteAllRecord(Human.class, Collections.<FilterOption>emptyList()));
        getTransactionService().complete();

        // Ensure in a separate transaction that they are correctly deleted
        getTransactionService().begin();
        assertNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human1.getId())));
        assertNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human2.getId())));
        getTransactionService().complete();
    }

    @Test
    public void deleteByIdsInSameTransaction() throws Exception {
        getTransactionService().begin();

        final Human human1 = PersistenceTestUtil.buildHuman("parent1FN", "parent1LN", 45);
        recorder.recordInsert(new InsertRecord(human1), null);

        final Human human2 = PersistenceTestUtil.buildHuman("parent2FN", "parent2LN", 54);
        recorder.recordInsert(new InsertRecord(human2), null);

        assertNotNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human1.getId())));
        assertNotNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human2.getId())));

        recorder.recordDelete(new DeleteRecord(human1), null);
        recorder.recordDelete(new DeleteRecord(human2), null);

        assertNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human1.getId())));
        assertNull(persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human2.getId())));

        getTransactionService().complete();
    }

    @Test
    public void verySimpleStorage() throws Exception {
        getTransactionService().begin();

        final Human human = PersistenceTestUtil.buildHuman("parent1FN", "parent1LN", 45);
        recorder.recordInsert(new InsertRecord(human), null);

        PersistenceTestUtil.checkHuman(human, persistenceService.selectById(new SelectByIdDescriptor<Human>(Human.class, human.getId())));
        recorder.recordDelete(new DeleteRecord(human), null);

        getTransactionService().complete();
    }

    @Test
    public void getOnlyChildren() throws Exception {
        getTransactionService().begin();

        final Parent parent = PersistenceTestUtil.buildParent("paretnFN", "parentLN", 12);
        recorder.recordInsert(new InsertRecord(parent), null);
        final Child child = buildChild("child1FN", "child11LN", 45, parent);
        recorder.recordInsert(new InsertRecord(child), null);

        final QueryOptions queryOptions = new QueryOptions(0, 10, Child.class, "firstName", OrderByType.ASC);
        final List<Child> allChildren = persistenceService.selectList(new SelectListDescriptor<Child>("getAllChildren", null, Child.class, queryOptions));

        assertEquals(1, allChildren.size());
        assertEquals(child, allChildren.get(0));
        recorder.recordDelete(new DeleteRecord(child), null);
        recorder.recordDelete(new DeleteRecord(parent), null);

        getTransactionService().complete();
    }

    @Test
    public void getAllHumans() throws Exception {
        getTransactionService().begin();

        final Human human = PersistenceTestUtil.buildHuman("humanFN", "humanLN", 12);
        recorder.recordInsert(new InsertRecord(human), null);
        final Parent parent = PersistenceTestUtil.buildParent("paretnFN", "parentLN", 12);
        recorder.recordInsert(new InsertRecord(parent), null);
        final Child child = buildChild("child1FN", "child11LN", 45, parent);
        recorder.recordInsert(new InsertRecord(child), null);

        final QueryOptions queryOptions = new QueryOptions(0, 10, Human.class, "firstName", OrderByType.ASC);
        final List<Human> allHumans = persistenceService.selectList(new SelectListDescriptor<Human>("getAllHumans", null, Human.class, queryOptions));
        assertEquals(3, allHumans.size());

        recorder.recordDelete(new DeleteRecord(child), null);
        recorder.recordDelete(new DeleteRecord(parent), null);
        recorder.recordDelete(new DeleteRecord(human), null);

        getTransactionService().complete();
    }

    @Test
    public void orderByAsc() throws Exception {
        getTransactionService().begin();
        final Human human1 = PersistenceTestUtil.buildHuman("aaa1", "humanLN", 12);
        final Human human2 = PersistenceTestUtil.buildHuman("aaa2", "humanLN", 12);
        final Human human3 = PersistenceTestUtil.buildHuman("aaa3", "humanLN", 12);
        // insert them not in a sequential order
        recorder.recordInsert(new InsertRecord(human1), null);
        recorder.recordInsert(new InsertRecord(human3), null);
        recorder.recordInsert(new InsertRecord(human2), null);
        final SelectListDescriptor<Human> selectDescriptor = new SelectListDescriptor<Human>("getAllHumans", null, Human.class, new QueryOptions(Human.class,
                "firstName", OrderByType.ASC));
        final List<Human> allHumans = persistenceService.selectList(selectDescriptor);
        // 4 because of the default parent inserted when creating the schema
        assertEquals(3, allHumans.size());
        assertEquals(human1, allHumans.get(0));
        assertEquals(human2, allHumans.get(1));
        assertEquals(human3, allHumans.get(2));

        recorder.recordDelete(new DeleteRecord(human1), null);
        recorder.recordDelete(new DeleteRecord(human2), null);
        recorder.recordDelete(new DeleteRecord(human3), null);

        getTransactionService().complete();
    }

    @Test
    public void insertInBatch() throws Exception {
        getTransactionService().begin();

        final Human human1 = PersistenceTestUtil.buildHuman("aaa1", "humanLN", 12);
        final Human human2 = PersistenceTestUtil.buildHuman("aaa2", "humanLN", 12);
        final Human human3 = PersistenceTestUtil.buildHuman("aaa3", "humanLN", 12);
        recorder.recordBatchInsert(new BatchInsertRecord(Arrays.asList(human1, human2, human3)), null);

        getTransactionService().complete();

        getTransactionService().begin();
        final SelectListDescriptor<Human> selectDescriptor = new SelectListDescriptor<Human>("getAllHumans", null, Human.class, new QueryOptions(Human.class,
                "firstName", OrderByType.DESC));

        final List<Human> allHumans = persistenceService.selectList(selectDescriptor);

        assertEquals(3, allHumans.size());
        assertEquals(human3, allHumans.get(0));
        assertEquals(human2, allHumans.get(1));
        assertEquals(human1, allHumans.get(2));

        recorder.recordDelete(new DeleteRecord(human1), null);
        recorder.recordDelete(new DeleteRecord(human2), null);
        recorder.recordDelete(new DeleteRecord(human3), null);

        getTransactionService().complete();
    }

    @Test
    public void orderByDesc() throws Exception {
        getTransactionService().begin();

        final Human human1 = PersistenceTestUtil.buildHuman("aaa1", "humanLN", 12);
        final Human human2 = PersistenceTestUtil.buildHuman("aaa2", "humanLN", 12);
        final Human human3 = PersistenceTestUtil.buildHuman("aaa3", "humanLN", 12);

        // insert them not in a sequential order
        recorder.recordInsert(new InsertRecord(human1), null);
        recorder.recordInsert(new InsertRecord(human3), null);
        recorder.recordInsert(new InsertRecord(human2), null);

        final SelectListDescriptor<Human> selectDescriptor = new SelectListDescriptor<Human>("getAllHumans", null, Human.class, new QueryOptions(Human.class,
                "firstName", OrderByType.DESC));

        final List<Human> allHumans = persistenceService.selectList(selectDescriptor);

        assertEquals(3, allHumans.size());
        assertEquals(human3, allHumans.get(0));
        assertEquals(human2, allHumans.get(1));
        assertEquals(human1, allHumans.get(2));

        recorder.recordDelete(new DeleteRecord(human1), null);
        recorder.recordDelete(new DeleteRecord(human2), null);
        recorder.recordDelete(new DeleteRecord(human3), null);

        getTransactionService().complete();
    }

    @Test
    public void insertAndDelete() throws Exception {
        getTransactionService().begin();

        final Human human1 = PersistenceTestUtil.buildHuman("Homer", "Simpson", 42);

        recorder.recordInsert(new InsertRecord(human1), null);

        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>();
        orderByOptions.add(new OrderByOption(Human.class, "firstName", OrderByType.ASC));
        orderByOptions.add(new OrderByOption(Human.class, "lastName", OrderByType.DESC));

        final SelectListDescriptor<Human> selectDescriptor = new SelectListDescriptor<Human>("getAllHumans", null, Human.class,
                new QueryOptions(orderByOptions));
        List<Human> allHumans;
        allHumans = persistenceService.selectList(selectDescriptor);
        assertEquals(1, allHumans.size());
        assertEquals(human1, allHumans.get(0));

        recorder.recordDelete(new DeleteRecord(human1), null);

        getTransactionService().complete();

        getTransactionService().begin();
        allHumans = persistenceService.selectList(selectDescriptor);
        assertEquals(0, allHumans.size());
        getTransactionService().complete();
    }

    @Test
    public void multiOrderBy() throws Exception {
        getTransactionService().begin();

        final Human human1 = PersistenceTestUtil.buildHuman("aaa3", "bbb1", 12);
        final Human human2 = PersistenceTestUtil.buildHuman("aaa2", "bbb2", 12);
        final Human human3 = PersistenceTestUtil.buildHuman("aaa3", "bbb3", 12);

        // insert them not in a sequential order
        recorder.recordInsert(new InsertRecord(human1), null);
        recorder.recordInsert(new InsertRecord(human3), null);
        recorder.recordInsert(new InsertRecord(human2), null);

        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>();
        orderByOptions.add(new OrderByOption(Human.class, "firstName", OrderByType.ASC));
        orderByOptions.add(new OrderByOption(Human.class, "lastName", OrderByType.DESC));

        final SelectListDescriptor<Human> selectDescriptor = new SelectListDescriptor<Human>("getAllHumans", null, Human.class,
                new QueryOptions(orderByOptions));
        final List<Human> allHumans = persistenceService.selectList(selectDescriptor);
        assertEquals(3, allHumans.size());
        assertEquals(human2, allHumans.get(0));
        assertEquals(human3, allHumans.get(1));
        assertEquals(human1, allHumans.get(2));

        recorder.recordDelete(new DeleteRecord(human1), null);
        recorder.recordDelete(new DeleteRecord(human2), null);
        recorder.recordDelete(new DeleteRecord(human3), null);

        getTransactionService().complete();
    }

    @Test
    public void orderByOtherTable() throws Exception {
        // checks if we can order by a column on another table than the object we retrieve
        // example: retrieve all parents order by child.firstname
        getTransactionService().begin();

        final Parent parent1 = PersistenceTestUtil.buildParent("parentFN1", "parentLN", 32);
        final Parent parent2 = PersistenceTestUtil.buildParent("parentFN2", "parentLN", 32);
        // insert parent2 & children first to have a different order inthe db than in the expected result of the select query
        recorder.recordInsert(new InsertRecord(parent2), null);
        recorder.recordInsert(new InsertRecord(parent1), null);

        final Child child11 = buildChild("child11", "bbb1", 12, parent1);
        final Child child12 = buildChild("cFN", "bbb1", 12, parent1);
        final Child child21 = buildChild("cFN", "bbb2", 12, parent2);
        final Child child22 = buildChild("child22", "bbb2", 12, parent2);
        recorder.recordInsert(new InsertRecord(child21), null);
        recorder.recordInsert(new InsertRecord(child22), null);
        recorder.recordInsert(new InsertRecord(child11), null);
        recorder.recordInsert(new InsertRecord(child12), null);

        final Map<String, Object> inputParameters = new HashMap<String, Object>();
        inputParameters.put("firstName", "cFN");
        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>();
        orderByOptions.add(new OrderByOption(Parent.class, "firstName", OrderByType.ASC));
        final SelectListDescriptor<Parent> selectDescriptor = new SelectListDescriptor<Parent>("getParentsHavingAChildWithFirstName", inputParameters,
                Parent.class, new QueryOptions(orderByOptions));
        final List<Parent> allParents = persistenceService.selectList(selectDescriptor);

        assertEquals(2, allParents.size());
        assertEquals(parent1, allParents.get(0));
        assertEquals(parent2, allParents.get(1));

        recorder.recordDelete(new DeleteRecord(child11), null);
        recorder.recordDelete(new DeleteRecord(child12), null);
        recorder.recordDelete(new DeleteRecord(child21), null);
        recorder.recordDelete(new DeleteRecord(child22), null);
        recorder.recordDelete(new DeleteRecord(parent1), null);
        recorder.recordDelete(new DeleteRecord(parent2), null);

        getTransactionService().complete();
    }

    @Test
    public void cascade() throws Exception {
        getTransactionService().begin();

        final Car car = buildCar("bmw");
        recorder.recordInsert(new InsertRecord(car), null);
        final Human human = PersistenceTestUtil.buildHuman("JackJackJack", "humanLN", 45);
        human.setCarId(car.getId());
        recorder.recordInsert(new InsertRecord(human), null);

        final SelectOneDescriptor<Car> carDescriptor = new SelectOneDescriptor<Car>("getCarsByBrand", CollectionUtil.buildSimpleMap("brand", "bmw"), Car.class);
        final SelectOneDescriptor<Human> humanDescriptor = new SelectOneDescriptor<Human>("getHumanByFirstName", CollectionUtil.buildSimpleMap("firstName",
                "JackJackJack"), Human.class);

        assertNotNull(persistenceService.selectOne(humanDescriptor));
        assertNotNull(persistenceService.selectOne(carDescriptor));
        getTransactionService().complete();

        getTransactionService().begin();
        recorder.recordDelete(new DeleteRecord(car), null);
        getTransactionService().complete();

        getTransactionService().begin();

        assertNull(persistenceService.selectOne(humanDescriptor));
        assertNull(persistenceService.selectOne(carDescriptor));

        getTransactionService().complete();
    }

    @Test
    public void readEntityField() throws Exception {
        getTransactionService().begin();

        final Human human = PersistenceTestUtil.buildHuman("parent1FN", "parent1LN", 45);
        recorder.recordInsert(new InsertRecord(human), null);
        final String firstName = persistenceService.selectOne(new SelectOneDescriptor<String>("getHumanFirstName", PersistenceTestUtil.getMap("id",
                human.getId()), Human.class, String.class));
        assertEquals("parent1FN", firstName);
        recorder.recordDelete(new DeleteRecord(human), null);

        getTransactionService().complete();
    }

    @Test
    public void getNumberOf() throws Exception {
        getTransactionService().begin();

        final Human human = PersistenceTestUtil.buildHuman("humanFN", "humanLN", 12);
        recorder.recordInsert(new InsertRecord(human), null);
        final Parent parent = PersistenceTestUtil.buildParent("paretnFN", "parentLN", 12);
        recorder.recordInsert(new InsertRecord(parent), null);
        final Child child = buildChild("child1FN", "child11LN", 45, parent);
        recorder.recordInsert(new InsertRecord(child), null);

        assertEquals(Long.valueOf(1), persistenceService.selectOne(new SelectOneDescriptor<Long>("getNumberOfParents", null, Parent.class, Long.class)));
        assertEquals(Long.valueOf(1), persistenceService.selectOne(new SelectOneDescriptor<Long>("getNumberOfChildren", null, Child.class, Long.class)));
        assertEquals(Long.valueOf(3), persistenceService.selectOne(new SelectOneDescriptor<Long>("getNumberOfHumans", null, Human.class, Long.class)));

        recorder.recordDelete(new DeleteRecord(child), null);
        recorder.recordDelete(new DeleteRecord(parent), null);
        recorder.recordDelete(new DeleteRecord(human), null);

        getTransactionService().complete();
    }

    @Test
    public void updateEntityField() throws Exception {
        getTransactionService().begin();

        final Human human = PersistenceTestUtil.buildHuman("parent1FN", "parent1LN", 45);
        recorder.recordInsert(new InsertRecord(human), null);
        assertEquals("parent1FN", persistenceService.selectOne(new SelectOneDescriptor<String>("getHumanFirstName", PersistenceTestUtil.getMap("id",
                human.getId()), Human.class, String.class)));
        recorder.recordUpdate(UpdateRecord.buildSetFields(human, Collections.<String, Object>singletonMap("firstName", "new")), null);
        assertEquals("new", human.getFirstName());
        assertEquals("new", persistenceService.selectOne(new SelectOneDescriptor<String>("getHumanFirstName", PersistenceTestUtil.getMap("id", human.getId()),
                Human.class, String.class)));
        recorder.recordDelete(new DeleteRecord(human), null);

        getTransactionService().complete();
    }

    @Test
    public void updateEntityFieldToEmpty() throws Exception {
        getTransactionService().begin();

        final Human human = PersistenceTestUtil.buildHuman("parent1FN", "parent1LN", 45);
        recorder.recordInsert(new InsertRecord(human), null);
        assertEquals("parent1FN", persistenceService.selectOne(new SelectOneDescriptor<String>("getHumanFirstName", PersistenceTestUtil.getMap("id",
                human.getId()), Human.class, String.class)));
        recorder.recordUpdate(UpdateRecord.buildSetFields(human, Collections.<String, Object>singletonMap("firstName", "")), null);
        assertEquals("", human.getFirstName());

        final String firstName = persistenceService.selectOne(new SelectOneDescriptor<String>("getHumanFirstName", PersistenceTestUtil.getMap("id",
                human.getId()), Human.class, String.class));
        // Oracle returns null instead of an empty string
        assertTrue(firstName == null || "".equals(firstName));
        recorder.recordDelete(new DeleteRecord(human), null);

        getTransactionService().complete();
    }

    @Test
    public void updateEntityFieldToNull() throws Exception {
        getTransactionService().begin();
        final Human human = PersistenceTestUtil.buildHuman("parent1FN", "parent1LN", 45);
        recorder.recordInsert(new InsertRecord(human), null);
        assertEquals("parent1FN", persistenceService.selectOne(new SelectOneDescriptor<String>("getHumanFirstName", PersistenceTestUtil.getMap("id",
                human.getId()), Human.class, String.class)));
        recorder.recordUpdate(UpdateRecord.buildSetFields(human, Collections.<String, Object>singletonMap("firstName", null)), null);
        assertNull(human.getFirstName());
        assertNull(persistenceService.selectOne(new SelectOneDescriptor<String>("getHumanFirstName", PersistenceTestUtil.getMap("id", human.getId()),
                Human.class, String.class)));
        recorder.recordDelete(new DeleteRecord(human), null);

        getTransactionService().complete();
    }

    @Test
    public void updateEntityFields() throws Exception {
        getTransactionService().begin();

        final Human human = PersistenceTestUtil.buildHuman("parent1FN", "parent1LN", 45);
        recorder.recordInsert(new InsertRecord(human), null);

        assertEquals("parent1FN", persistenceService.selectOne(new SelectOneDescriptor<String>("getHumanFirstName", PersistenceTestUtil.getMap("id",
                human.getId()), Human.class, String.class)));

        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("firstName", "newFN");
        fields.put("age", 12);

        recorder.recordUpdate(UpdateRecord.buildSetFields(human, fields), null);
        assertEquals("newFN", human.getFirstName());
        assertEquals(12, human.getAge());

        final Human updatedHuman = persistenceService.selectOne(new SelectOneDescriptor<Parent>("getHumanById",
                PersistenceTestUtil.getMap("id", human.getId()), Parent.class));
        assertEquals("newFN", updatedHuman.getFirstName());
        assertEquals(12, updatedHuman.getAge());

        recorder.recordDelete(new DeleteRecord(human), null);

        getTransactionService().complete();
    }

    @Test
    public void parentRelation() throws Exception {
        getTransactionService().begin();

        final Parent parent = PersistenceTestUtil.buildParent("parent1FN", "parent1LN", 45);
        recorder.recordInsert(new InsertRecord(parent), null);

        final Child child1 = buildChild("child1FN", "child1LN", 12, parent);
        final Child child2 = buildChild("child2FN", "child2LN", 12, parent);

        recorder.recordInsert(new InsertRecord(child1), null);
        recorder.recordInsert(new InsertRecord(child2), null);

        checkParent(parent, persistenceService.selectById(new SelectByIdDescriptor<Parent>(Parent.class, parent.getId())));
        checkChild(child1, persistenceService.selectById(new SelectByIdDescriptor<Child>(Child.class, child1.getId())));
        final Map<String, Object> inputParameters = Collections.singletonMap("id", (Object) child1.getId());
        checkParent(parent, persistenceService.selectOne(new SelectOneDescriptor<Human>("getChildParent", inputParameters, Human.class)));

        final List<Child> readChildren = persistenceService.selectList(new SelectListDescriptor<Child>("getParentChildren", PersistenceTestUtil.getMap("id",
                parent.getId()), Child.class, new QueryOptions(0, 20, Child.class, "id", OrderByType.ASC)));
        assertNotNull(readChildren);
        assertEquals(2, readChildren.size()); // postgres do not retrieve children with the query in the good order
        boolean child1Ok = false;
        boolean child2Ok = false;
        for (final Child child : readChildren) {
            if (child1.getId() == child.getId()) {
                checkChild(child1, child);
                child1Ok = true;
                break;
            }
        }
        for (final Child child : readChildren) {
            if (child2.getId() == child.getId()) {
                checkChild(child2, child);
                child2Ok = true;
                break;
            }
        }
        assertTrue("does not retrieved good children", child1Ok && child2Ok);
        final List<Human> humansByFirstName = persistenceService.selectList(new SelectListDescriptor<Human>("getHumanByFirstName", PersistenceTestUtil.getMap(
                "firstName", child1.getFirstName()), Human.class, new QueryOptions(0, 20, Human.class, "id", OrderByType.ASC)));
        assertNotNull(humansByFirstName);
        assertEquals(1, humansByFirstName.size());
        PersistenceTestUtil.checkHuman(child1, humansByFirstName.iterator().next());
        recorder.recordDelete(new DeleteRecord(parent), null);

        getTransactionService().complete();
    }

    @Test
    public void getListWithCollectionParameter() throws Exception {
        getTransactionService().begin();

        final Parent parent = PersistenceTestUtil.buildParent("parent1FN", "parent1LN", 45);

        recorder.recordInsert(new InsertRecord(parent), null);

        final Child child1 = buildChild("child1FN", "child1LN", 12, parent);
        final Child child2 = buildChild("child2FN", "child2LN", 12, parent);
        recorder.recordInsert(new InsertRecord(child1), null);
        recorder.recordInsert(new InsertRecord(child2), null);

        getTransactionService().complete();

        getTransactionService().begin();
        final ArrayList<Long> list = new ArrayList<Long>();
        list.add(PersistenceTestUtil.buildHuman(child1).getId());
        list.add(PersistenceTestUtil.buildHuman(child2).getId());
        list.add(PersistenceTestUtil.buildHuman(parent).getId());

        // final List<?> list2 = persistenceService.readList(new SelectDescriptor<Human>("getHumanByIds", TenantUtil.getMap("ids", list), Human.class));
        final List<Human> list2 = persistenceService.selectList(new SelectListDescriptor<Human>("getHumansById", PersistenceTestUtil.getMap("ids", list),
                Human.class, new QueryOptions(0, 20, Human.class, "id", OrderByType.ASC)));
        assertEquals(3, list2.size());

        final Child readChild1 = persistenceService.selectById(new SelectByIdDescriptor<Child>(Child.class, child1.getId()));
        final Child readChild2 = persistenceService.selectById(new SelectByIdDescriptor<Child>(Child.class, child2.getId()));
        final Child readParent = persistenceService.selectById(new SelectByIdDescriptor<Parent>(Parent.class, parent.getId()));

        assertTrue(list2.contains(readChild1));
        assertTrue(list2.contains(readChild2));
        assertTrue(list2.contains(readParent));

        recorder.recordDelete(new DeleteRecord(child1), null);
        recorder.recordDelete(new DeleteRecord(child2), null);
        recorder.recordDelete(new DeleteRecord(parent), null);

        assertNull(persistenceService.selectById(new SelectByIdDescriptor<Child>(Child.class, child1.getId())));
        assertNull(persistenceService.selectById(new SelectByIdDescriptor<Child>(Child.class, child2.getId())));
        assertNull(persistenceService.selectById(new SelectByIdDescriptor<Parent>(Parent.class, parent.getId())));

        getTransactionService().complete();
    }

    private void checkParent(final Parent expected, final Human actual) {
        PersistenceTestUtil.checkHuman(expected, actual);
        assertEquals(Parent.class, actual.getClass());
    }

    private void checkChild(final Child expected, final Human actual) {
        PersistenceTestUtil.checkHuman(expected, actual);
        assertEquals(Child.class, actual.getClass());
        final Child actualChild = (Child) actual;
        assertEquals(expected.getParentId(), actualChild.getParentId());
    }

    private Child buildChild(final String firstName, final String lastName, final int age, final Parent parent) {
        final Child child = new Child();
        child.setFirstName(firstName);
        child.setLastName(lastName);
        child.setAge(age);
        child.setParentId(parent.getId());
        return child;
    }

    private Car buildCar(final String brand) {
        final Car car = new Car();
        car.setBrand(brand);
        return car;
    }

    @Test
    public void filterResults() throws Exception {
        getTransactionService().begin();

        final Human human1 = PersistenceTestUtil.buildHuman("Matti", "Makela", 27);
        final Human human2 = PersistenceTestUtil.buildHuman("Eikki", "Nieminen", 29);
        final Human human3 = PersistenceTestUtil.buildHuman("Pekka", "Salmi", 27);

        // insert them not in a sequential order
        recorder.recordInsert(new InsertRecord(human1), null);
        recorder.recordInsert(new InsertRecord(human3), null);
        recorder.recordInsert(new InsertRecord(human2), null);

        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>();
        orderByOptions.add(new OrderByOption(Human.class, "firstName", OrderByType.ASC));
        orderByOptions.add(new OrderByOption(Human.class, "lastName", OrderByType.DESC));

        final List<FilterOption> filters = new ArrayList<FilterOption>();
        filters.add(new FilterOption(Human.class, "age", 27));

        final QueryOptions queryOptions = new QueryOptions(0, 2, orderByOptions, filters, null);
        final SelectListDescriptor<Human> selectDescriptor = new SelectListDescriptor<Human>("getAllHumans", null, Human.class, queryOptions);
        final List<Human> humans = persistenceService.selectList(selectDescriptor);
        assertEquals(2, humans.size());
        assertEquals(human1, humans.get(0));
        assertEquals(human3, humans.get(1));

        recorder.recordDelete(new DeleteRecord(human1), null);
        recorder.recordDelete(new DeleteRecord(human2), null);
        recorder.recordDelete(new DeleteRecord(human3), null);

        getTransactionService().complete();
    }

    @Test
    public void filterSearchResults() throws Exception {
        getTransactionService().begin();

        final Human human1 = PersistenceTestUtil.buildHuman("Matti", "Makela", 27);
        final Human human2 = PersistenceTestUtil.buildHuman("Eikki", "Nieminen", 29);
        final Human human3 = PersistenceTestUtil.buildHuman("Pekka", "Salmi", 27);

        // insert them not in a sequential order
        recorder.recordInsert(new InsertRecord(human1), null);
        recorder.recordInsert(new InsertRecord(human3), null);
        recorder.recordInsert(new InsertRecord(human2), null);

        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>();
        orderByOptions.add(new OrderByOption(Human.class, "firstName", OrderByType.ASC));
        orderByOptions.add(new OrderByOption(Human.class, "lastName", OrderByType.DESC));

        final List<FilterOption> filters = new ArrayList<FilterOption>(1);
        filters.add(new FilterOption(Human.class, "age", 27));
        final Map<Class<? extends PersistentObject>, Set<String>> fields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> set = new HashSet<String>(2);
        set.add("firstName");
        set.add("lastName");
        fields.put(Human.class, set);
        final SearchFields searchFields = new SearchFields(Arrays.asList("Mak"), fields);

        final QueryOptions queryOptions = new QueryOptions(0, 2, orderByOptions, filters, searchFields);
        final SelectListDescriptor<Human> selectDescriptor = new SelectListDescriptor<Human>("getAllHumans", null, Human.class, queryOptions);
        final List<Human> humans = persistenceService.selectList(selectDescriptor);
        assertEquals(1, humans.size());
        assertEquals(human1, humans.get(0));

        recorder.recordDelete(new DeleteRecord(human1), null);
        recorder.recordDelete(new DeleteRecord(human2), null);
        recorder.recordDelete(new DeleteRecord(human3), null);
        getTransactionService().complete();
    }

    @Test
    public void search_Humans_With_Fields_Starting_With_Prefix() throws Exception {
        getTransactionService().begin();

        final Human human = PersistenceTestUtil.buildHuman("humanFN", "humanLN", 24);
        recorder.recordInsert(new InsertRecord(human), null);
        final Parent parent = PersistenceTestUtil.buildParent("parentFN", "parentLN", 36);
        recorder.recordInsert(new InsertRecord(parent), null);
        final Child child = buildChild("child1FN", "child11LN", 5, parent);
        recorder.recordInsert(new InsertRecord(child), null);

        final QueryOptions queryOptions = buildQueryOptionsOrderByFirstnameASC("hum");

        final List<Human> allHumans = persistenceService.selectList(new SelectListDescriptor<Human>("getAllHumans", null, Human.class, queryOptions));

        recorder.recordDelete(new DeleteRecord(child), null);
        recorder.recordDelete(new DeleteRecord(parent), null);
        recorder.recordDelete(new DeleteRecord(human), null);

        getTransactionService().complete();

        assertEquals(1, allHumans.size());
    }

    protected QueryOptions buildQueryOptionsOrderByFirstnameASC(final String searchTerm) {
        final Map<Class<? extends PersistentObject>, Set<String>> allFields = new HashMap<Class<? extends PersistentObject>, Set<String>>();
        final Set<String> fields = new HashSet<String>(2);
        fields.add("firstName");
        fields.add("lastName");
        allFields.put(Human.class, fields);
        final QueryOptions queryOptions = new QueryOptions(0, 10, Arrays.asList(new OrderByOption(Human.class, "firstName", OrderByType.ASC)),
                new ArrayList<FilterOption>(0), new SearchFields(Arrays.asList(searchTerm), allFields));
        return queryOptions;
    }

}
