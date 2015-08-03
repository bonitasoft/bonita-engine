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

import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mazourd
 */
public class MapAdapterExpression extends XmlAdapter<MapAdapterExpression.AdaptedMapbis, Map<String, Expression>> {

    public static class AdaptedMapbis {

        @XmlElement(name = "input")
        public List<Entry> entry = new ArrayList<Entry>();

    }

    public static class Entry {

        @XmlAttribute(name = "name")
        public String key;
        @XmlElement(type = ExpressionImpl.class, name = "expression")
        public Expression value;

    }

    @Override
    public Map<String, Expression> unmarshal(AdaptedMapbis adaptedMap) throws Exception {
        Map<String, Expression> map = new HashMap<String, Expression>();
        for (Entry entry : adaptedMap.entry) {
            map.put(entry.key, entry.value);
        }
        return map;
    }

    @Override
    public AdaptedMapbis marshal(Map<String, Expression> map) throws Exception {
        AdaptedMapbis adaptedMap = new AdaptedMapbis();
        for (Map.Entry<String, Expression> mapEntry : map.entrySet()) {
            Entry entry = new Entry();
            entry.key = mapEntry.getKey();
            entry.value = mapEntry.getValue();
            adaptedMap.entry.add(entry);
        }
        return adaptedMap;
    }

}
