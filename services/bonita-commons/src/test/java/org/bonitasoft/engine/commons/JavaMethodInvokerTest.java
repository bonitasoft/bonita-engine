/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.commons;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class JavaMethodInvokerTest {

    @Test
    public void invokeJavaMethodShouldNotModifyObjectReference() throws Exception {
        // given:
        final JavaMethodInvoker invoker = new JavaMethodInvoker();
        final User user = new User("Jo la frite");
        final String initialUserReference = user.toString();

        // when:
        invoker.invokeJavaMethod(String.class.getName(), "Manu", user, "setName", String.class.getName());

        // then:
        final String newUserReference = user.toString();
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

    public class MyClass {

        private int thing = 0;

        public void setThing(final int thing) {
            this.thing = thing;
        }

        public int getThing() {
            return thing;
        }

    }

    @Test
    public void invokeJavaMethod_should_update_a_list() throws Exception {
        final JavaMethodInvoker invoker = new JavaMethodInvoker();
        final List<User> users = new ArrayList<User>();
        final List<User> createdUsers = new ArrayList<User>();
        createdUsers.add(new User("Matti"));

        invoker.invokeJavaMethod(createdUsers.getClass().getName(), createdUsers, users, "addAll", List.class.getName());

        assertThat(users).isEqualTo(createdUsers);
    }

    @Test
    public void invokeJavaMethod_should_use_autoboxing() throws Exception {
        final MyClass myData = new MyClass();

        final JavaMethodInvoker invoker = new JavaMethodInvoker();

        final MyClass object = (MyClass) invoker.invokeJavaMethod(Integer.class.getName(), Integer.valueOf(83), myData, "setThing", "int");

        assertThat(object.getThing()).isEqualTo(83);
    }

}
