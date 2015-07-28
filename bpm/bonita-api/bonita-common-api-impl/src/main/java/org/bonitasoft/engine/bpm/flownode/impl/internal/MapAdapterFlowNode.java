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

import org.bonitasoft.engine.bpm.flownode.FlowNodeDefinition;
import org.bonitasoft.engine.bpm.process.impl.internal.SubProcessDefinitionImpl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mazourd
 */
public class MapAdapterFlowNode extends XmlAdapter<MapAdapterFlowNode.AdaptedMap, Map<String, FlowNodeDefinition>> {

    public static class AdaptedMap {

        public List<Entrybis> entry = new ArrayList<Entrybis>();

    }

    public static class Entrybis {

        public String key;
        @XmlElements({
                @XmlElement(type = EndEventDefinitionImpl.class),
                @XmlElement(type = StartEventDefinitionImpl.class),
                @XmlElement(type = IntermediateCatchEventDefinitionImpl.class),
                @XmlElement(type = IntermediateThrowEventDefinitionImpl.class),
                @XmlElement(type = StartEventDefinitionImpl.class),
                @XmlElement(type = AutomaticTaskDefinitionImpl.class),
                @XmlElement(type = CallActivityDefinitionImpl.class),
                @XmlElement(type = ManualTaskDefinitionImpl.class),
                @XmlElement(type = ReceiveTaskDefinitionImpl.class),
                @XmlElement(type = SendTaskDefinitionImpl.class),
                @XmlElement(type = UserTaskDefinitionImpl.class),
                @XmlElement(type = BoundaryEventDefinitionImpl.class),
                @XmlElement(type = GatewayDefinitionImpl.class),
                @XmlElement(type = SubProcessDefinitionImpl.class)
        })
        public FlowNodeDefinition value;

    }

    @Override
    public Map<String, FlowNodeDefinition> unmarshal(AdaptedMap adaptedMap) throws Exception {
        Map<String, FlowNodeDefinition> map = new HashMap<String, FlowNodeDefinition>();
        for (Entrybis entry : adaptedMap.entry) {
            map.put(entry.key, entry.value);
        }
        return map;
    }

    @Override
    public AdaptedMap marshal(Map<String, FlowNodeDefinition> map) throws Exception {
        AdaptedMap adaptedMap = new AdaptedMap();
        for (Map.Entry<String, FlowNodeDefinition> mapEntry : map.entrySet()) {
            Entrybis entry = new Entrybis();
            entry.key = mapEntry.getKey();
            entry.value = mapEntry.getValue();
            adaptedMap.entry.add(entry);
        }
        return adaptedMap;
    }

}
