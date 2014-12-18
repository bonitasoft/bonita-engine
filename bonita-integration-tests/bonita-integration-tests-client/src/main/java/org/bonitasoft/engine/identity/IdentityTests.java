package org.bonitasoft.engine.identity;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    UserIT.class,
    RoleIT.class,
    GroupIT.class,
    MembershipIT.class,
    OrganizationIT.class,
    CustomUserInfoIT.class
})
public class IdentityTests {

}
