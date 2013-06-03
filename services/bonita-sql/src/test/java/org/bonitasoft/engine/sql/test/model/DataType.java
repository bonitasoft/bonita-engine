package org.bonitasoft.engine.sql.test.model;

import java.math.BigDecimal;
import java.sql.Blob;
import java.util.Date;

public class DataType {

  private int id;
  private Integer intCol;
  private boolean booleanCol;
  private BigDecimal decimalCol;
  private Double doubleCol;
  private float floatCol;
  private Date timeCol;
  private Date dateCol;
  private Date datetimeCol;
  private String varcharCol;
  private String charCol;
  private byte[] binaryCol;
  private Blob blobCol;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Integer getIntCol() {
    return intCol;
  }

  public void setIntCol(Integer intCol) {
    this.intCol = intCol;
  }

  public boolean isBooleanCol() {
    return booleanCol;
  }

  public void setBooleanCol(boolean booleanCol) {
    this.booleanCol = booleanCol;
  }

  public BigDecimal getDecimalCol() {
    return decimalCol;
  }

  public void setDecimalCol(BigDecimal decimalCol) {
    this.decimalCol = decimalCol;
  }

  public Double getDoubleCol() {
    return doubleCol;
  }

  public void setDoubleCol(Double doubleCol) {
    this.doubleCol = doubleCol;
  }

  public float getFloatCol() {
    return floatCol;
  }

  public void setFloatCol(float floatCol) {
    this.floatCol = floatCol;
  }

  public Date getTimeCol() {
    return timeCol;
  }

  public void setTimeCol(Date timeCol) {
    this.timeCol = timeCol;
  }

  public Date getDateCol() {
    return dateCol;
  }

  public void setDateCol(Date dateCol) {
    this.dateCol = dateCol;
  }

  public Date getDatetimeCol() {
    return datetimeCol;
  }

  public void setDatetimeCol(Date datetimeCol) {
    this.datetimeCol = datetimeCol;
  }

  public String getVarcharCol() {
    return varcharCol;
  }

  public void setVarcharCol(String varcharCol) {
    this.varcharCol = varcharCol;
  }

  public String getCharCol() {
    return charCol;
  }

  public void setCharCol(String charCol) {
    this.charCol = charCol;
  }

  public byte[] getBinaryCol() {
    return binaryCol;
  }

  public void setBinaryCol(byte[] binaryCol) {
    this.binaryCol = binaryCol;
  }

  public Blob getBlobCol() {
    return blobCol;
  }

  public void setBlobCol(Blob blobCol) {
    this.blobCol = blobCol;
  }

}
