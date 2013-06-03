package org.bonitasoft.engine.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.junit.Test;

public class DOMWriterTest {

  private static final String currentDir = System.getProperty("user.dir");
  
  @Test
  public void test() throws IOException, TransformerConfigurationException, ParserConfigurationException {
    Person john = new Person("John", "Doe");
    Person jane = new Person("Jane", "Doe");

    AddressBook addressBook = new AddressBook("family", "1.0");
    addressBook.addPerson(jane);
    addressBook.addPerson(john);

    XMLNode johnNode = new XMLNode("person");
    XMLNode johnLastName = new XMLNode("lastName");
    johnLastName.setContent(john.getLastName());
    XMLNode johnFirstName = new XMLNode("firstName");
    johnFirstName.setContent(john.getFirstName());
    johnNode.addChild(johnFirstName);
    johnNode.addChild(johnLastName);

    XMLNode janeNode = new XMLNode("person");
    XMLNode janeLastName = new XMLNode("lastName");
    janeLastName.setContent(jane.getLastName());
    XMLNode janeFirstName = new XMLNode("firstName");
    janeFirstName.setContent(jane.getFirstName());
    janeNode.addChild(janeFirstName);
    janeNode.addChild(janeLastName);

    XMLNode persons= new XMLNode("persons");
    persons.addChild(johnNode);
    persons.addChild(janeNode);

    XMLNode rootNode = new XMLNode("addressbook");
    rootNode.addAttribute("name", addressBook.getName());
    rootNode.addAttribute("version", addressBook.getVersion());
    rootNode.addChild(persons);

    final StringBuilder pathBuiler = new StringBuilder(currentDir);
    pathBuiler.append(File.separator).append("target").append(File.separator)
    .append(addressBook.getName()).append(".xml");

    FileOutputStream fos = new FileOutputStream(new File(pathBuiler.toString()));
    DOMWriter writer = new DOMWriter(null);
    writer.write(rootNode, fos);
    fos.close();
  }

}
