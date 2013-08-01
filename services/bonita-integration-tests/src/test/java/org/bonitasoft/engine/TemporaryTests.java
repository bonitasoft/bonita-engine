package org.bonitasoft.engine;

import org.bonitasoft.engine.persistence.TenantTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TenantTest.class })
public class TemporaryTests extends AllTestsWithJNDI {

}
