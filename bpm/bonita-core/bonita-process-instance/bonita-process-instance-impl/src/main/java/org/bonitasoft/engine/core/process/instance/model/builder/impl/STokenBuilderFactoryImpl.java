/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.core.process.instance.model.builder.STokenBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.STokenBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.impl.STokenImpl;

/**
 * @author Celine Souchet
 */
public class STokenBuilderFactoryImpl implements STokenBuilderFactory {

    @Override
    public STokenBuilder createNewInstance(final long processInstanceId) {
        NullCheckingUtil.checkArgsNotNull(processInstanceId);
        final STokenImpl entity = new STokenImpl(processInstanceId);
        return new STokenBuilderImpl(entity);
    }

}
