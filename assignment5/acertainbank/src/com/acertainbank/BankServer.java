/**
 * 
 */
package com.acertainbank;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Starts the HTTP server that the proxies will communicate with.
 */
public class BankServer {

  /**
   * @param args
   */
  public static void main(String[] args) throws ConfigurationException {

    String server_port_string = args[1];
    String partitionId_string = args[2];
    int port;
    int partitionId;

    try {
      port = Integer.parseInt(server_port_string);
      partitionId = Integer.parseInt(partitionId_string);
    } catch(NumberFormatException ex) {
      throw new ConfigurationException(ex.getMessage());
    }

    Handler accMan = new Handler(partitionId, "../../../config.xml");
    Server server = new Server(port);
    server.setHandler(accMan);

    try {
      server.start();
      server.join();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}
