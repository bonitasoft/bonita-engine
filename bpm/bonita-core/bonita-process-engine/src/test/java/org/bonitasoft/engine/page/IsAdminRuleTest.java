package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class IsAdminRuleTest {

    IsAdminRule isAdminRule = new IsAdminRule();

    @Test
    public void isAllowed_should_return_true_if_admin_in_context() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put(AuthorizationRuleConstants.IS_ADMIN, true);

        assertThat(isAdminRule.isAllowed("key", context)).isTrue();
    }

    @Test
    public void isAllowed_should_return_false_if_not_admin_in_context() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put(AuthorizationRuleConstants.IS_ADMIN, false);

        assertThat(isAdminRule.isAllowed("key", context)).isFalse();
    }

    @Test
    public void isAllowed_should_return_false_if_empty_context() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();

        assertThat(isAdminRule.isAllowed("key", context)).isFalse();
    }

    @Test
    public void getIdShouldReturnIsAdmin() throws Exception {
        assertThat(isAdminRule.getId()).isEqualTo("IS_ADMIN");
    }
}
