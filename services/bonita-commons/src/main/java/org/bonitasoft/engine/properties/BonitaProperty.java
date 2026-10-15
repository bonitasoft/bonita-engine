/**
 * Copyright (C) 2025 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.properties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * Annotation to be used on fields to inject a property value from the environment.
 * It works very much like the <code>&#064;Value</code> annotation from Spring.
 * Can be provided with a list of deprecated property names. If so, when used, the annotation processor will first look
 * for the
 * deprecated properties and if found, will log a warning.
 * Eg:
 *
 * <pre class="code">
 *
 * &#064;BonitaProperty(name = "my.property", deprecated = { "old.my.property", "older.my.property" })
 * private String myProperty;
 * </pre>
 *
 * In simple cases where no deprecated properties are defined, the name attribute can be omitted (and passed a default
 * unnamed 'value' attribute):
 *
 * <pre class="code">
 *
 * &#064;BonitaProperty("my.property")
 * private String myProperty;
 * </pre>
 *
 * @see BonitaPropertyAnnotationProcessor how this Annotation is processed
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface BonitaProperty {

    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    String[] deprecated() default {};
}
