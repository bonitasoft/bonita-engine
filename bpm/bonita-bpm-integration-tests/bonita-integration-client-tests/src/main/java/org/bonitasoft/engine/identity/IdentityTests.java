package org.bonitasoft.engine.identity;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    UserTest.class,
    RoleTest.class,
    GroupTest.class,
    MembershipTest.class,
    OrganizationTest.class,
    CustomUserInfoIT.class
})
public class IdentityTests {

}
