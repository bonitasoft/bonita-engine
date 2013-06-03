package org.bonitasoft.engine.persistence;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    TenantTest.class,
    MultiTenancyTest.class
})
public class PersistenceTests {

}
