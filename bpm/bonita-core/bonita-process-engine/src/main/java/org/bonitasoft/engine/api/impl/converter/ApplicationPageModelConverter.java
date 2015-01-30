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
package org.bonitasoft.engine.api.impl.converter;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.business.application.impl.ApplicationPageImpl;
import org.bonitasoft.engine.business.application.model.SApplicationPage;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationPageModelConverter {

    public ApplicationPage toApplicationPage(final SApplicationPage sApplicationPage) {
        final ApplicationPageImpl appPage = new ApplicationPageImpl(sApplicationPage.getApplicationId(), sApplicationPage.getPageId(),
                sApplicationPage.getToken());
        appPage.setId(sApplicationPage.getId());
        return appPage;
    }

    public List<ApplicationPage> toApplicationPage(final List<SApplicationPage> sApplicationPages) {
        final List<ApplicationPage> appPages = new ArrayList<ApplicationPage>(sApplicationPages.size());
        for (final SApplicationPage sApplicationPage : sApplicationPages) {
            appPages.add(toApplicationPage(sApplicationPage));
        }
        return appPages;
    }
}
