/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.profile.builder.impl;

import org.bonitasoft.engine.profile.builder.SProfileBuilder;
import org.bonitasoft.engine.profile.builder.SProfileBuilderAccessor;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilder;
import org.bonitasoft.engine.profile.builder.SProfileEntryUpdateBuilder;
import org.bonitasoft.engine.profile.builder.SProfileMemberBuilder;
import org.bonitasoft.engine.profile.builder.SProfileMemberUpdateBuilder;
import org.bonitasoft.engine.profile.builder.SProfileUpdateBuilder;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SProfileBuilderAccessorImpl implements SProfileBuilderAccessor {

    @Override
    public SProfileBuilder getSProfileBuilder() {
        return new SProfileBuilderImpl();
    }

    @Override
    public SProfileEntryBuilder getSProfileEntryBuilder() {
        return new SProfileEntryBuilderImpl();
    }

    @Override
    public SProfileMemberBuilder getSProfileMemberBuilder() {
        return new SProfileMemberBuilderImpl();
    }

    @Override
    public SProfileUpdateBuilder getSProfileUpdateBuilder() {
        return new SProfileUpdateBuilderImpl();
    }

    @Override
    public SProfileEntryUpdateBuilder getSProfileEntryUpdateBuilder() {
        return new SProfileEntryUpdateBuilderImpl();
    }

    @Override
    public SProfileMemberUpdateBuilder getSProfileMemberUpdateBuilder() {
        return new SProfileMemberUpdateBuilderImpl();
    }

}
