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

package org.bonitasoft.engine.bpm.process.impl.internal;

import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

/**
 * @author mazourd
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class IndexLabel {
    @XmlAttribute
    private String index;
    @XmlAttribute
    private String label;
    @XmlElement(type = ExpressionImpl.class)
    private Expression value;

    public IndexLabel (String index,String label, Expression value){
        this.index = index;
        this.value = value;
        this.label = label;
    }
    public IndexLabel (){
        this.index = "-1";
        this.value = null;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexLabel that = (IndexLabel) o;
        return Objects.equals(index, that.index) &&
                Objects.equals(label, that.label) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, label, value);
    }

    @Override
    public String toString() {
        return "IndexLabel{" +
                "index='" + index + '\'' +
                ", label='" + label + '\'' +
                ", value=" + value +
                '}';
    }
}
