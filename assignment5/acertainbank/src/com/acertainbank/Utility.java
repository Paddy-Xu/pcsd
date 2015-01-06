package com.acertainbank;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
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

	public static Object deserializeFromXml(String xmlString) {
		XStream xmlStream = new XStream(new StaxDriver());
		Object object = xmlStream.fromXML(xmlString);
		return object;
	}

  public static Response rpc(HttpClient client, ContentExchange exchange)
      throws CommunicationException {
    int state;
    try {
		  client.send(exchange);
      state = exchange.waitForDone();
    } catch (IOException err) {
      throw new CommunicationException(err.getMessage());
    } catch (InterruptedException err) {
      throw new CommunicationException(err.getMessage());
    }
		if (state == HttpExchange.STATUS_COMPLETED) {
      try {
  			return (Response) Utility.deserializeFromXml(
  			    exchange.getResponseContent().trim());
      } catch (UnsupportedEncodingException err) {
        throw new CommunicationException(err.getMessage());
      }
		} else {
			throw new CommunicationException();
		}
  }

}
