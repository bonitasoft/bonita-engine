package org.bonitasoft.engine.expression.impl;

public class Order {

    private final String shippingAddress;

    private final long referenceNumber;

    public Order(final String shippingAddress, final long referenceNumber) {
        super();
        this.shippingAddress = shippingAddress;
        this.referenceNumber = referenceNumber;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public long getReferenceNumber() {
        return referenceNumber;
    }

}
