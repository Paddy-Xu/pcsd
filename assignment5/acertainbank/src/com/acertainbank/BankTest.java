package com.acertainbank;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class BankTest {

  private static AccountManagerHTTPProxy proxy;
  private static Handler handler0, handler1;

  public BankTest() {}

  @BeforeClass
  public static void beforeClass() throws ConfigurationException {
    proxy = new AccountManagerHTTPProxy("config.xml");
    handler0 = new Handler(0, "config.xml");
    handler1 = new Handler(1, "config.xml");
  }

  @Test
  public void testGetParam() {
    String uri = "?herp=derp&derp=herp";
    assertEquals("derp", Utility.getParam(uri, "herp"));
    assertEquals("herp", Utility.getParam(uri, "derp"));
    assertEquals("", Utility.getParam(uri, "kek"));
  }

}
