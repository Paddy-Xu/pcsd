package com.acertainbank;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class CertainBankTest {

  public CertainBankTest() {}

  @Test
  public void testGetParam() {
    String uri = "?herp=derp&derp=herp";
    assertEquals("derp", Utility.getParam(uri, "herp"));
    assertEquals("herp", Utility.getParam(uri, "derp"));
    assertEquals("", Utility.getParam(uri, "kek"));
  }

}
