package com.acertainbank;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Utility {

  static public String readFile(String path, Charset encoding)
      throws IOException {
    byte[] data = Files.readAllBytes(Paths.get(path));
    return new String(data, encoding);
  }

  static public Element readXmlFile(String path)
      throws IOException, ParserConfigurationException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(path);
    return document.getDocumentElement();
  }

}
