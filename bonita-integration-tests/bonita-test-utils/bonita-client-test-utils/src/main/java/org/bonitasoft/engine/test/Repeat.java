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
package org.bonitasoft.engine.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Laurent Leseigneur
 *         <p>use this rule to play x times the same junit test and add rule {@link RepeatRule}.</p>
 *         Example:<br>
 *        <pre>
 * {@code
 * @Rule public RepeatRule repeatRule = new RepeatRule();
 * @Repeat(times = 100)
 *               public void testName() throws Exception {
 *               ...
 *               }
 *               }
 *               </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
    java.lang.annotation.ElementType.METHOD
})
public @interface Repeat {

    public abstract int times();
}
