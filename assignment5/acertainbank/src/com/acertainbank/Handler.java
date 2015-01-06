package com.acertainbank;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class Handler extends AbstractHandler {

  private final HashMap<Integer, Branch> branches =
      new HashMap<Integer, Branch>();

  public Handler(int partitionId, String configFile)
      throws ConfigurationException {
    Element config;
    try {
      config = Utility.readXmlFile(configFile);
    } catch (IOException err) {
      throw new ConfigurationException(err.getMessage());
    } catch (ParserConfigurationException err) {
      throw new ConfigurationException(err.getMessage());
    } catch (SAXException err) {
      throw new ConfigurationException(err.getMessage());
    }
    // Parse branch configuration
		NodeList branchList = config.getElementsByTagName("Branch");
		for (int i = 0; i < branchList.getLength(); ++i) {
			Node branch = branchList.item(i);
			Integer branchPartition = Integer.parseInt(
					branch.getAttributes().getNamedItem("partitionId").getNodeValue());
			if (branchPartition != partitionId) {
				continue;
			}
			Integer branchId = Integer.parseInt(
					branch.getAttributes().getNamedItem("id").getNodeValue());
			if (branchId < 0) {
				throw new ConfigurationException(
				    "Invalid branch id specified for branch" + branchId);
			}
			branches.put(branchId, new Branch());
		}
  }

	public void handle(String target, Request baseRequest,
			               HttpServletRequest request,
                     HttpServletResponse httpResponse)
			throws IOException, ServletException {

    Response response = new Response();

		httpResponse.setContentType("text/html;charset=utf-8");
		httpResponse.setStatus(HttpServletResponse.SC_OK);
		String uri = request.getRequestURI().toLowerCase();

    if (uri.startsWith("/credit")) {

    } else {
      response.setError(new CommunicationException("Unknown request"));
    }

    try {
      httpResponse.getWriter().println(Utility.serializeToXml(response));
    } catch (IOException err) {
      // Send back an empty response
    }
    baseRequest.setHandled(true);
  }

}
