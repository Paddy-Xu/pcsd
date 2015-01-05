package com.acertainbank;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

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

	public static String serializeToXml(Object object) {
		XStream xmlStream = new XStream(new StaxDriver());
		String xmlString = xmlStream.toXML(object);
		return xmlString;
	}

	/**
	 * De-serializes an xml string to object
	 *
	 * @param xmlObject
	 * @return
	 */
	public static Object deserializeFromXml(String xmlString) {
		XStream xmlStream = new XStream(new StaxDriver());
		Object object = xmlStream.fromXML(xmlString);
		return object;
	}

}
