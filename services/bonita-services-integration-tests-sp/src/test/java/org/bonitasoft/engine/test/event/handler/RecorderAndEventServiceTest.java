/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.test.event.handler;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.archive.test.model.Employee;
import org.bonitasoft.engine.archive.test.model.SEmployeeHandlerImpl;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.ServicesBuilder;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.BusinessTransaction;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Elias Ricken de Medeiros
 */
public class RecorderAndEventServiceTest {

    private static TransactionService txService;

    private static ServicesBuilder servicesBuilder;

    private static PlatformService platformService;

    private static SPlatformBuilder platformBuilder;

    private static STenantBuilder tenantBuilder;

    private static SessionAccessor sessionAccessor;

    private static EventService eventService;

    private static SessionService sessionService;

    private static PersistenceService persistenceService;

    private static Recorder recorder;

    static {
        servicesBuilder = new ServicesBuilder();
        txService = servicesBuilder.buildTransactionService();
        platformService = servicesBuilder.buildPlatformService();
        platformBuilder = servicesBuilder.buildPlatformBuilder();
        tenantBuilder = servicesBuilder.buildTenantBuilder();
        sessionAccessor = servicesBuilder.buildSessionAccessor();
        eventService = servicesBuilder.buildEventService();
        sessionService = servicesBuilder.buildSessionService();
        persistenceService = servicesBuilder.buildPersistence();
        recorder = servicesBuilder.buildRecorder(false);
    }

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        private final Logger LOGGER = LoggerFactory.getLogger(RecorderAndEventServiceTest.class);

        @Override
        public void starting(final Description d) {
            LOGGER.error("Starting test: " + this.getClass().getName() + "." + d.getMethodName());
        }

        @Override
        public void failed(final Throwable cause, final Description d) {
            LOGGER.error("Failed test: " + this.getClass().getName() + "." + d.getMethodName());
        }

        @Override
        public void succeeded(final Description d) {
            LOGGER.error("Succeeded test: " + this.getClass().getName() + "." + d.getMethodName());
        }
    };

    @BeforeClass
    public static void setUpPersistence() throws Exception {
        TestUtil.createPlatformAndDefaultTenant(txService, platformService, sessionAccessor, platformBuilder, tenantBuilder, sessionService);
        TestUtil.startScheduler(servicesBuilder.buildSchedulerService());
    }

    @After
    public void clean() throws SBonitaException {
        TestUtil.closeTransactionIfOpen(txService);
    }

    @AfterClass
    public static void tearDownPersistence() throws Exception {
        TestUtil.stopScheduler(servicesBuilder.buildSchedulerService());
        TestUtil.deleteDefaultTenantAndPlatForm(txService, platformService, sessionAccessor, sessionService);
    }

    @Test
    public void testUpdateEmployeeName() throws Exception {
        // Add an Employee using persistence service
        BusinessTransaction btx = txService.createTransaction();
        btx.begin();
        final Employee employee = new Employee("John", 15);
        persistenceService.insert(employee);
        btx.complete();

        // Update an Employee using recorder
        btx = txService.createTransaction();
        btx.begin();

        // Make UpdateRecord parameter
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("name", "Alice");
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(employee, fields);

        // Make SUpdateEvent parameter
        final String eventType = getEventType(employee);
        final SUpdateEvent updateEvent = (SUpdateEvent) eventService.getEventBuilder().createUpdateEvent(eventType).setObject(updateRecord.getEntity()).done();

        final Employee oldEmployee = new Employee(employee);
        updateEvent.setOldObject(oldEmployee);

        final SEmployeeHandlerImpl employeeHandler = resetEmployeeHandler(eventService, eventType + "_UPDATED");

        recorder.recordUpdate(updateRecord, updateEvent);
        assertEquals(true, employeeHandler.isUpdated());

        btx.complete();
    }

    @Test
    public void testNotUpdateEmployeeName() throws Exception {
        // Add an Employee using persistence service
        BusinessTransaction btx = txService.createTransaction();
        btx.begin();
        final Employee employee = new Employee("John", 15);
        persistenceService.insert(employee);
        btx.complete();

        // Update an Employee using recorder
        btx = txService.createTransaction();
        btx.begin();

        // Make UpdateRecord parameter
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("age", 20);
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(employee, fields);

        // Make SUpdateEvent parameter
        final String eventType = getEventType(employee);
        final SUpdateEvent updateEvent = (SUpdateEvent) eventService.getEventBuilder().createUpdateEvent(eventType).setObject(updateRecord.getEntity()).done();

        final Employee oldEmployee = new Employee(employee);
        updateEvent.setOldObject(oldEmployee);

        final SEmployeeHandlerImpl employeeHandler = resetEmployeeHandler(eventService, eventType + "_UPDATED");

        recorder.recordUpdate(updateRecord, updateEvent);

        // assert if employeeHandler is executed
        assertEquals(false, employeeHandler.isUpdated());

        btx.complete();
    }

    private SEmployeeHandlerImpl resetEmployeeHandler(final EventService updateEventService, final String eventType) {
        final SEmployeeHandlerImpl employeeHandler = (SEmployeeHandlerImpl) updateEventService.getHandlers(eventType).toArray()[0];
        employeeHandler.setUpdated(false);
        return employeeHandler;
    }

    private String getEventType(final Employee employee) {
        final String discriminator = employee.getDiscriminator();
        return discriminator.substring(discriminator.lastIndexOf(".") + 1, discriminator.length()).toUpperCase();
    }
}
