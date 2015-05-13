/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
 */

package org.bonitasoft.engine.core.contract.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;

import org.bonitasoft.engine.data.instance.model.impl.XStreamFactory;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class SProcessContractDataTest {

    @Test
    public void setValue_serialize_to_xml() throws Exception {
        final SProcessContractData saProcessContractData = new SProcessContractData();

        final MyPojo myPojo = new MyPojo("theName", "theValue".getBytes());
        saProcessContractData.setValue(myPojo);


        final Serializable value = saProcessContractData.value;
        assertThat(value).isInstanceOf(String.class);
        final Serializable unserializedValue = saProcessContractData.getValue();
        assertThat(unserializedValue).isInstanceOf(MyPojo.class);
        assertThat(((MyPojo) unserializedValue).getName()).isEqualTo("theName");
        assertThat(((MyPojo) unserializedValue).getContent()).isEqualTo("theValue".getBytes());
        assertThat(value).isEqualTo(XStreamFactory.getXStream().toXML(myPojo));
    }

}