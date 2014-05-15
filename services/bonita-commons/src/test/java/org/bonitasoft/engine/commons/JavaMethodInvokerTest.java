package org.bonitasoft.engine.commons;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class JavaMethodInvokerTest {

    @Test
    public void invokeJavaMethodShouldNotModifyObjectReference() throws Exception {
        // given:
        JavaMethodInvoker invoker = new JavaMethodInvoker();
        User user = new User("Jo la frite");
        String initialUserReference = user.toString();

        // when:
        invoker.invokeJavaMethod(String.class.getName(), "Manu", user, "setName", String.class.getName());

        // then:
        String newUserReference = user.toString();
        assertThat(newUserReference).isEqualTo(initialUserReference);
    }

    class User {

        private String name;

        public User(final String name) {
            this.name = name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }
}
