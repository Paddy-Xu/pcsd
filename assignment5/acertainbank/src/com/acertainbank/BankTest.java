package com.acertainbank;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BankTest {

  private static final String CONFIG_FILE = "config.xml";
  private static final File SERVER_LOG = new File("server.log");
  private static AccountManagerHTTPProxy proxy;
  private static final ArrayList<Process> servers = new ArrayList<Process>();

  public BankTest() {}

  private static Process spawnHandler(int port, int partitionId)
      throws IOException {
    String javaBin = System.getProperty("java.home") +
                     File.separator + "bin" +
                     File.separator + "java";
    String classPath = System.getProperty("java.class.path");
    ProcessBuilder builder =
        new ProcessBuilder(javaBin, "-cp", classPath,
                           "com.acertainbank.BankServer",
                           Integer.toString(port),
                           Integer.toString(partitionId));

    builder.redirectOutput(SERVER_LOG);
    builder.redirectError(SERVER_LOG);
    return builder.start();
  }

  @BeforeClass
  public static void beforeClass() throws ConfigurationException {
    proxy = new AccountManagerHTTPProxy(CONFIG_FILE);
    Element config;
    try {
      config = Utility.readXmlFile(CONFIG_FILE);
    } catch (IOException err) {
      throw new ConfigurationException(err.getMessage());
    } catch (ParserConfigurationException err) {
      throw new ConfigurationException(err.getMessage());
    } catch (SAXException err) {
      throw new ConfigurationException(err.getMessage());
    }
    // Launch local servers if specified in config file
		NodeList partitionList = config.getElementsByTagName("Partition");
		for (int i = 0; i < partitionList.getLength(); ++i) {
			NamedNodeMap partition = partitionList.item(i).getAttributes();
      String address = partition.getNamedItem("address").getNodeValue();
      if (!address.startsWith("http://localhost")) continue;
      int port =
          Integer.parseInt(partition.getNamedItem("port").getNodeValue());
      try {
        servers.add(spawnHandler(port, i));
      } catch (IOException err) {
        throw new ConfigurationException(err.getMessage());
      }
		}
  }

  @AfterClass
  public static void afterClass() {
    for (Process p : servers) {
      p.destroy();
    }
    servers.clear();
    proxy = null;
  }

  @Test
  public void testGetParam() {
    String uri = "?herp=derp&derp=herp";
    assertEquals("derp", Utility.getParam(uri, "herp"));
    assertEquals("herp", Utility.getParam(uri, "derp"));
    assertEquals("", Utility.getParam(uri, "kek"));
  }

  @Test
  public void testMethods() throws Exception {
    proxy.debit(0, 0, 3000);
    assertTrue(-3000 == proxy.calculateExposure(0));
    proxy.transfer(0, 3, 0, 2000);
    assertTrue(0 == proxy.calculateExposure(-1000));
  }

}
