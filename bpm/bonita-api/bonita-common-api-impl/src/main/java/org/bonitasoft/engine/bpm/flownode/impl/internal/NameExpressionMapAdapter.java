/*******************************************************************************
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
 ******************************************************************************/

package org.bonitasoft.engine.bpm.flownode.impl.internal;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.bonitasoft.engine.expression.Expression;

/**
 * @author mazourd
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class NameExpressionMapAdapter extends XmlAdapter<NameExpressionMap, Map<String, Expression>> {

    @Override
    public Map<String, Expression> unmarshal(NameExpressionMap nameExpressionMap) throws Exception {
        Map<String, Expression> map = new HashMap<>();
        for (NameExpressionPair nameExpressionPair : nameExpressionMap.nameExpressionPair) {
            map.put(nameExpressionPair.key, nameExpressionPair.value);
        }
        return map;
    }

    @Override
    public NameExpressionMap marshal(Map<String, Expression> map) throws Exception {
        NameExpressionMap nameExpressionMap = new NameExpressionMap();
        for (Map.Entry<String, Expression> mapEntry : map.entrySet()) {
            NameExpressionPair pair = new NameExpressionPair();
            pair.key = mapEntry.getKey();
            pair.value = mapEntry.getValue();
            nameExpressionMap.nameExpressionPair.add(pair);
        }
        return nameExpressionMap;
    }

}
