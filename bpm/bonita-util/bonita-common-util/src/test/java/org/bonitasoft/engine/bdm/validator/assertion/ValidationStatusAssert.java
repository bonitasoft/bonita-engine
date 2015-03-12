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
package org.bonitasoft.engine.bdm.validator.assertion;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import org.bonitasoft.engine.bdm.validator.ValidationStatus;

public class ValidationStatusAssert extends AbstractAssert<ValidationStatusAssert, ValidationStatus> {

    protected ValidationStatusAssert(ValidationStatus actual) {
        super(actual, ValidationStatusAssert.class);
    }

    public static ValidationStatusAssert assertThat(ValidationStatus actual) {
        return new ValidationStatusAssert(actual);
    }

    public ValidationStatusAssert isOk() {
        Assertions.assertThat(actual.isOk()).isTrue();
        return this;
    }

    public ValidationStatusAssert isOk(String description) {
        Assertions.assertThat(actual.isOk()).as(description).isTrue();
        return this;
    }

    public ValidationStatusAssert isNotOk() {
        Assertions.assertThat(actual.isOk()).isFalse();
        return this;
    }

    public ValidationStatusAssert isNotOk(String description) {
        Assertions.assertThat(actual.isOk()).as(description).isFalse();
        return this;
    }
}
