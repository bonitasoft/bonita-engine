package org.bonitasoft.engine.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.monitoring.mbean.MBeanStartException;
import org.bonitasoft.engine.monitoring.mbean.MBeanStopException;
import org.bonitasoft.engine.scheduler.JobParameterBuilder;
import org.bonitasoft.engine.scheduler.SJobDescriptor;
import org.bonitasoft.engine.scheduler.SJobParameter;
import org.bonitasoft.engine.scheduler.SSchedulerException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.Trigger;
import org.bonitasoft.engine.transaction.SBadTransactionStateException;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class TenantMonitoringServiceTest extends CommonServiceTest {

    private final TenantMonitoringService monitoringSvc;

    private static IdentityService identityService;

    private static IdentityModelBuilder identityModelBuilder;

    private static SchedulerService schedulerService;

    private static MBeanServer mbserver = null;

    private static ObjectName entityMB;

    private static ObjectName serviceMB;

    private final String password = "password";

    protected abstract TenantMonitoringService getMonitoringService() throws Exception;

    public TenantMonitoringServiceTest() throws Exception {
        monitoringSvc = getMonitoringService();
    }

    static {
        identityService = getServicesBuilder().buildIdentityService();
        identityModelBuilder = getServicesBuilder().buildIdentityModelBuilder();
        schedulerService = getServicesBuilder().buildSchedulerService();
    }

    @Before
    public void setup() throws Exception {

        final ArrayList<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
        if (mbservers.size() > 0) {
            mbserver = mbservers.get(0);
        }
        if (mbserver == null) {
            mbserver = MBeanServerFactory.createMBeanServer();
        }

        final long tenantId = getSessionAccessor().getTenantId();
        // Constructs the mbean names
        entityMB = new ObjectName(TenantMonitoringService.ENTITY_MBEAN_PREFIX + tenantId);
        serviceMB = new ObjectName(TenantMonitoringService.SERVICE_MBEAN_PREFIX + tenantId);

        unregisterMBeans();
    }

    @After
    public void tearDown() throws Exception {
        // complete active transaction if assertion fails
        try {
            getTransactionService().complete();
        } catch (final STransactionCommitException e) {
            // OK
        } catch (final STransactionRollbackException e) {
            // OK
        }
    }

    /**
     * Assure that no Bonitasoft MBeans are registered in the MBServer before each test.
     * 
     * @throws MBeanRegistrationException
     * @throws InstanceNotFoundException
     */

    public void unregisterMBeans() throws MBeanRegistrationException, InstanceNotFoundException {
        if (mbserver.isRegistered(entityMB)) {
            mbserver.unregisterMBean(entityMB);
        }
        if (mbserver.isRegistered(serviceMB)) {
            mbserver.unregisterMBean(serviceMB);
        }
    }

    @Test
    public void startMbeanAccessibility() throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanStartException {
        assertFalse(mbserver.isRegistered(entityMB));
        assertFalse(mbserver.isRegistered(serviceMB));

        monitoringSvc.registerMBeans();

        assertTrue(mbserver.isRegistered(entityMB));
        assertTrue(mbserver.isRegistered(serviceMB));
    }

    @Test
    public void stopMbeanAccessibility() throws MBeanStartException, MBeanStopException {

        monitoringSvc.registerMBeans();
        assertTrue(mbserver.isRegistered(entityMB));
        assertTrue(mbserver.isRegistered(serviceMB));

        monitoringSvc.unregisterMbeans();
        assertFalse(mbserver.isRegistered(entityMB));
        assertFalse(mbserver.isRegistered(serviceMB));
    }

    @Test
    public void getNumberOfUsers() throws Exception {

        final long before = getNumberOfUsersFromMonitoringService();
        final SUser user = createNewUser("firstUser", password);
        long after = getNumberOfUsersFromMonitoringService();

        // check if the number of user retrieved has been increased by one
        assertEquals(1, after - before);

        deleteUser(user);
        after = getNumberOfUsersFromMonitoringService();
        assertEquals(0, after - before);

    }

    private long getNumberOfUsersFromMonitoringService() throws STransactionCommitException, STransactionCreationException, SBadTransactionStateException,
            FireEventException, SMonitoringException, STransactionRollbackException {
        return getNumberOfUsersFromMonitoringService(monitoringSvc);
    }

    private void deleteUser(final SUser user) throws Exception {
        getTransactionService().begin();
        // delete the previously created user
        identityService.deleteUser(user);
        // end the transaction
        getTransactionService().complete();
    }

    private long getNumberOfUsersFromMonitoringService(final TenantMonitoringService monitoringService) throws STransactionCreationException,
            SBadTransactionStateException, FireEventException, SMonitoringException, STransactionCommitException, STransactionRollbackException {
        getTransactionService().begin();
        // fetch the number of users
        final long nbOfUsers = monitoringService.getNumberOfUsers();
        getTransactionService().complete();
        return nbOfUsers;
    }

    public SUser createNewUser(final String username, final String password) throws Exception {
        final SUserBuilder userBuilder = identityModelBuilder.getUserBuilder().createNewInstance().setUserName(username).setPassword(password);
        getTransactionService().begin();
        final SUser user = identityService.createUser(userBuilder.done());
        getTransactionService().complete();
        return user;
    }

    @Test
    public void getActiveTransactionTest() throws Exception {

        // assertEquals(0, svcMB.getActiveTransactionNb());
        assertEquals(0, monitoringSvc.getNumberOfActiveTransactions());

        // create a transaction
        getTransactionService().begin();
        // check the transaction has been successfully counted
        assertEquals(1, monitoringSvc.getNumberOfActiveTransactions());
        // close the transaction
        getTransactionService().complete();

        assertEquals(0, monitoringSvc.getNumberOfActiveTransactions());
    }

    @Test
    public void getExecutingJobsTest() throws Exception {
        final long startNbOfExecutingJobs = monitoringSvc.getNumberOfExecutingJobs();
        assertEquals(0, startNbOfExecutingJobs);

        final String theResponse = "theUltimateQuestionOfLifeTheUniverseAndEverything";
        // create an action that will schedule a job
        final VariableStorageForMonitoring storage = VariableStorageForMonitoring.getInstance();
        storage.setVariable(theResponse, 42);

        final SchedulerService schdSvc = schedulerService;
        // stop and start the Scheduler service to clean the previous job list
        getTransactionService().begin();

        schdSvc.start();
        final Date now = new Date();
        scheduleJob(theResponse, schdSvc, now);
        getTransactionService().complete();

        final WaitFor waitForJobExecuting = new WaitFor(50, 5000) {

            @Override
            boolean check() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
                // return svcMB.getExecutingJobsNb() == (startNbOfExecutingJobs + 1);
                return monitoringSvc.getNumberOfExecutingJobs() == startNbOfExecutingJobs + 1;
            }
        };

        // check the number of executing job has incremented
        assertTrue(waitForJobExecuting.waitFor());

        // set the storage variable to 1 to finish the Job execution
        storage.setVariable(theResponse, 1);
        // wait while the job finish its execution
        final WaitFor waitforJobCompleted = new WaitFor(50, 5000) {

            @Override
            boolean check() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
                // return (svcMB.getExecutingJobsNb() == startNbOfExecutingJobs);
                return monitoringSvc.getNumberOfExecutingJobs() == startNbOfExecutingJobs;
            }

        };

        // check the number of executing jobs is 0
        assertTrue(waitforJobCompleted.waitFor());

        schdSvc.shutdown();
    }

    private void scheduleJob(final String theResponse, final SchedulerService schdSvc, final Date now) throws SSchedulerException, FireEventException {
        final SJobDescriptor jobDescriptor = schdSvc.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.monitoring.IncrementAVariable", "IncrementAVariable").setDescription("increment a variable").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final JobParameterBuilder jobParameterBuilder = schdSvc.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("variableName", theResponse).done());
        final Trigger trigger = new OneShotTrigger("events", now, 10);
        schdSvc.schedule(jobDescriptor, parameters, trigger);
    }

}
