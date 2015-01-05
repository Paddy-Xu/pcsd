package com.acertainbank;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

class CertainBankTest {

  public static void main(String args[])
      throws ConfigurationException, IOException, ParserConfigurationException,
             SAXException {
    AccountManagerHTTPProxy proxy = new AccountManagerHTTPProxy("config.xml");
  }

}
