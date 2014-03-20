package org.bonitasoft.engine.identity.model.impl;

import java.math.BigDecimal;

public class Test {

    private Long longObject;

    private long longPrimitive;

    private BigDecimal bigDecimal;

    private float floatPrimitive;

    private Float floatObject;

    private double doublePrimitive;

    private Double doubleObject;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (bigDecimal == null ? 0 : bigDecimal.hashCode());
        result = prime * result + (doubleObject == null ? 0 : doubleObject.hashCode());
        long temp;
        temp = Double.doubleToLongBits(doublePrimitive);
        result = prime * result + (int) (temp ^ temp >>> 32);
        result = prime * result + (floatObject == null ? 0 : floatObject.hashCode());
        result = prime * result + Float.floatToIntBits(floatPrimitive);
        result = prime * result + (longObject == null ? 0 : longObject.hashCode());
        result = prime * result + (int) (longPrimitive ^ longPrimitive >>> 32);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Test other = (Test) obj;
        if (bigDecimal == null) {
            if (other.bigDecimal != null) {
                return false;
            }
        } else if (!bigDecimal.equals(other.bigDecimal)) {
            return false;
        }
        if (doubleObject == null) {
            if (other.doubleObject != null) {
                return false;
            }
        } else if (!doubleObject.equals(other.doubleObject)) {
            return false;
        }
        if (Double.doubleToLongBits(doublePrimitive) != Double.doubleToLongBits(other.doublePrimitive)) {
            return false;
        }
        if (floatObject == null) {
            if (other.floatObject != null) {
                return false;
            }
        } else if (!floatObject.equals(other.floatObject)) {
            return false;
        }
        if (Float.floatToIntBits(floatPrimitive) != Float.floatToIntBits(other.floatPrimitive)) {
            return false;
        }
        if (longObject == null) {
            if (other.longObject != null) {
                return false;
            }
        } else if (!longObject.equals(other.longObject)) {
            return false;
        }
        if (longPrimitive != other.longPrimitive) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Test [longObject=" + longObject + ", longPrimitive=" + longPrimitive + ", bigDecimal=" + bigDecimal + ", floatPrimitive=" + floatPrimitive
                + ", floatObject=" + floatObject + ", doublePrimitive=" + doublePrimitive + ", doubleObject=" + doubleObject + "]";
    }

}
