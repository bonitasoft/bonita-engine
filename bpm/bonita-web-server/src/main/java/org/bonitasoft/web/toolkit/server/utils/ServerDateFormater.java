/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.toolkit.server.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bonitasoft.web.toolkit.client.common.CommonDateFormater;

/**
 * @author Paul AMAR
 */
// TODO : remove all reference in rest-server to DateFormat, Modifier, Reader, etc... Then delete this class
public class ServerDateFormater extends CommonDateFormater {

    /**
     * Default Constructor.
     */
    public ServerDateFormater() {
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.web.toolkit.client.common.CommonDateFormater#_parse(java.lang.String, java.lang.String)
     */
    @Override
    public Date _parse(final String value, final String format) {
        Date d = null;
        final SimpleDateFormat formatter = new SimpleDateFormat(format);
        try {
            d = formatter.parse(value);
        } catch (final ParseException e) {
            // Exception thrown by parse method
            e.printStackTrace();
        }
        return d;
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.web.toolkit.client.common.CommonDateFormater#_toString(java.util.Date, java.lang.String)
     */
    @Override
    public String _toString(final Date value, final String format) {
        final SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(value);
    }

}
