/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.services.event.handler;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.archive.model.Employee;
import org.bonitasoft.engine.archive.model.SEmployeeHandlerImpl;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.AfterClass;
import org.junit.Test;

import com.bonitasoft.services.CommonServiceSPTest;

/**
 * @author Elias Ricken de Medeiros
 */
public class RecorderAndEventServiceTest extends CommonServiceSPTest {

    private static EventService eventService;

    private static PersistenceService persistenceService;

    private static Recorder recorder;

    static {
        eventService = getServicesBuilder().buildEventService();
        persistenceService = getServicesBuilder().buildTenantPersistenceService();
        recorder = getServicesBuilder().buildRecorder();
    }

    @AfterClass
    public static void tearDownPersistence() throws Exception {
        TestUtil.stopScheduler(getServicesBuilder().buildSchedulerService(), getTransactionService());
    }

    @Test
    public void testUpdateEmployeeName() throws Exception {
        // Add an Employee using persistence service
        getTransactionService().begin();
        Employee employee = new Employee("John", 15);
        persistenceService.insert(employee);
        getTransactionService().complete();

        // Update an Employee using recorder
        getTransactionService().begin();
        final SelectByIdDescriptor<Employee> selectByIdDescriptor = new SelectByIdDescriptor<Employee>("getEmployeeById", Employee.class, employee.getId());
        employee = persistenceService.selectById(selectByIdDescriptor);
        // Make UpdateRecord parameter
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("name", "Alice");
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(employee, fields);

        // Make SUpdateEvent parameter
        final String eventType = getEventType(employee);
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(eventType).setObject(updateRecord.getEntity()).done();

        final Employee oldEmployee = new Employee(employee);
        updateEvent.setOldObject(oldEmployee);

        final SEmployeeHandlerImpl employeeHandler = resetEmployeeHandler(eventService, eventType + "_UPDATED");

        recorder.recordUpdate(updateRecord, updateEvent);
        assertEquals(true, employeeHandler.isUpdated());

        getTransactionService().complete();
    }

    @Test
    public void testNotUpdateEmployeeName() throws Exception {
        // Add an Employee using persistence service
        getTransactionService().begin();
        Employee employee = new Employee("John", 15);
        persistenceService.insert(employee);
        getTransactionService().complete();

        // Update an Employee using recorder
        getTransactionService().begin();

        final SelectByIdDescriptor<Employee> selectByIdDescriptor = new SelectByIdDescriptor<Employee>("getEmployeeById", Employee.class, employee.getId());
        employee = persistenceService.selectById(selectByIdDescriptor);
        // Make UpdateRecord parameter
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("age", 20);
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(employee, fields);

        // Make SUpdateEvent parameter
        final String eventType = getEventType(employee);
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(eventType).setObject(updateRecord.getEntity()).done();

        final Employee oldEmployee = new Employee(employee);
        updateEvent.setOldObject(oldEmployee);

        final SEmployeeHandlerImpl employeeHandler = resetEmployeeHandler(eventService, eventType + "_UPDATED");

        recorder.recordUpdate(updateRecord, updateEvent);

        // assert if employeeHandler is executed
        assertEquals(false, employeeHandler.isUpdated());

        getTransactionService().complete();
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
