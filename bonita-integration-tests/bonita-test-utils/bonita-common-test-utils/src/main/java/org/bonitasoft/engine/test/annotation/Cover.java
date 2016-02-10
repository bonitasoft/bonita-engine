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
package org.bonitasoft.engine.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cover {

    public enum BPMNConcept {
        GATEWAY, EVENTS, EVENT_SUBPROCESS, ACTIVITIES, OTHERS, ORGANIZATION, PROCESS, CONNECTOR, NONE, MULTIINSTANCE, ACTOR, EXPRESSIONS, SUPERVISOR, PROFILE, MEMBER, SUB_TASK, OPERATION, DOCUMENT, DATA, CALL_ACTIVITY, APPLICATION
    }

    Class<?>[] classes();

    Class<?>[] exceptions() default {};

    BPMNConcept concept();

    String story() default "";

    String jira();

    String[] keywords();

}
