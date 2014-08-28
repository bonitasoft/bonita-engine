package org.bonitasoft.engine;

import org.bonitasoft.engine.archive.ArchiveServiceTest;
import org.bonitasoft.engine.authentication.AuthenticationServiceTest;
import org.bonitasoft.engine.cache.CacheServiceTest;
import org.bonitasoft.engine.classloader.ClassLoaderServiceTest;
import org.bonitasoft.engine.command.CommandServiceIntegrationTest;
import org.bonitasoft.engine.continuation.WorkServiceTest;
import org.bonitasoft.engine.data.instance.TransientDataInstanceServiceIT;
import org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImplIT;
import org.bonitasoft.engine.dependency.DependencyServiceTest;
import org.bonitasoft.engine.expression.ExpressionServiceTest;
import org.bonitasoft.engine.identity.IdentityServiceTest;
import org.bonitasoft.engine.persistence.PersistenceTests;
import org.bonitasoft.engine.platform.TenantManagementTest;
import org.bonitasoft.engine.platform.auth.PlatformAuthenticationServiceTest;
import org.bonitasoft.engine.platform.command.PlatformCommandServiceIntegrationTest;
import org.bonitasoft.engine.profile.ProfileServiceTest;
import org.bonitasoft.engine.recorder.RecorderTest;
import org.bonitasoft.engine.scheduler.impl.QuartzSchedulerExecutorITest;
import org.bonitasoft.engine.session.PlatformSessionServiceTest;
import org.bonitasoft.engine.session.SessionServiceTest;
import org.bonitasoft.engine.xml.ParserTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

// FIXME add platformtest suite
@RunWith(Suite.class)
@SuiteClasses({
        CacheServiceTest.class,
        PersistenceTests.class,
        ArchiveServiceTest.class,
        ClassLoaderServiceTest.class,
        ExpressionServiceTest.class,
        IdentityServiceTest.class,
        AuthenticationServiceTest.class,
        PlatformAuthenticationServiceTest.class,
        SessionServiceTest.class,
        PlatformSessionServiceTest.class,
        DataInstanceServiceImplIT.class,
        TransientDataInstanceServiceIT.class,
        DependencyServiceTest.class,
        WorkServiceTest.class,

        // -- SqlTest.class,
        // -- Tests using the scheduler
        RecorderTest.class,
        QuartzSchedulerExecutorITest.class,
        // JobTest.class, ignored as this was the last test method in that class
        CommandServiceIntegrationTest.class,
        // DocumentServiceTest.class,
        PlatformCommandServiceIntegrationTest.class,
        ProfileServiceTest.class,
        ParserTest.class,
        TenantManagementTest.class
})
/**
 * Do not run this test suite alone. Use AllTestsWithJNDI instead.
 * 
 * @author Emmanuel Duchastenier
 *
 */
public class AllTests {

}
