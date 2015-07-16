

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

package org.bonitasoft.engine.actor.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Matthieu Chaffotte
 */
@XmlRootElement(name = "actorMappings", namespace = "http://www.bonitasoft.org/ns/actormapping/6.0")
@XmlAccessorType(XmlAccessType.FIELD)
public class ActorMapping {

    //@XmlElementWrapper(name = "actor-mappings",required = true)
    @XmlElement(name = "actorMapping", required = false)
    private List<Actor> actors;

    public ActorMapping() {
        actors = new ArrayList<Actor>(10);
    }

    public List<Actor> getActors() {
        return actors;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    public void addActor(final Actor actor) {
        actors.add(actor);
    }

    @Override
    public String toString() {
        return "ActorMapping{" +
                "actors=" + actors +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ActorMapping that = (ActorMapping) o;
        return Objects.equals(actors, that.actors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actors);
    }
}
