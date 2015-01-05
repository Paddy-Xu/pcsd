package com.acertainbank;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

class CertainBankTest {

  public static void main(String args[]) throws Exception {
    AccountManagerHTTPProxy proxy = new AccountManagerHTTPProxy("config.xml");
  }

}
