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
package org.bonitasoft.web.rest.server.api;

import static org.bonitasoft.web.rest.server.framework.APIServletCall.PARAMETER_FILTER;

import java.util.List;

import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Global binding configuration for REST controllers.
 * <p>
 * Registers a {@link CustomCollectionEditor} for the {@code "f"} request parameter only, so that
 * comma-separated filter values are preserved as a single string instead of being split into a
 * list by Spring's default binding.
 * <p>
 * For example, with {@code ?f=names=Harry,Anna}:
 * <ul>
 * <li><b>"f" parameter</b>: bound as the single value {@code "names=Harry,Anna"} (desired behavior)</li>
 * </ul>
 * This editor is intentionally <b>not</b> applied to other list parameters (e.g. {@code ids}), where
 * the default comma-splitting is expected:
 * <ul>
 * <li><b>"ids" parameter</b>: {@code ?ids=1,2,3} is bound as three separate values
 * {@code ["1", "2", "3"]}</li>
 * </ul>
 */
@ControllerAdvice
public class FilterParameterBindingAdvice {

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        if (PARAMETER_FILTER.equals(binder.getObjectName())) {
            binder.registerCustomEditor(List.class, new CustomCollectionEditor(List.class));
        }
    }
}
