package com.acertainbank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class Handler extends AbstractHandler implements AccountManager {

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
      ArrayList<Account> branchAccounts = new ArrayList<Account>();
      if (branch.getNodeType() == Node.ELEMENT_NODE) {
        NodeList accounts = ((Element)branch).getElementsByTagName("Account");
        for (int j = 0; j < accounts.getLength(); ++j) {
          NamedNodeMap account = accounts.item(j).getAttributes();
          int accountId =
              Integer.parseInt(account.getNamedItem("id").getNodeValue());
          double balance = Double.parseDouble(
              account.getNamedItem("balance").getNodeValue());
          branchAccounts.add(new Account(accountId, balance));
        }
      }
			if (branchId < 0) {
				throw new ConfigurationException(
				    "Invalid branch id specified for branch" + branchId);
			}
			branches.put(branchId, new Branch(branchAccounts));
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
      int branchId =
          Integer.parseInt(Utility.getParam(uri, Constants.BRANCH_ID));
      int accountId =
          Integer.parseInt(Utility.getParam(uri, Constants.ACCOUNT_ID));
      double amount =
          Double.parseDouble(Utility.getParam(uri, Constants.AMOUNT));
      try {
        credit(branchId, accountId, amount);
      } catch (Exception err) {
        response.setError(err);
      }
    } else if (uri.startsWith("/debit")) {
      int branchId =
          Integer.parseInt(Utility.getParam(uri, Constants.BRANCH_ID));
      int accountId =
          Integer.parseInt(Utility.getParam(uri, Constants.ACCOUNT_ID));
      double amount =
          Double.parseDouble(Utility.getParam(uri, Constants.AMOUNT));
      try {
        debit(branchId, accountId, amount);
      } catch (Exception err) {
        response.setError(err);
      }
    } else if (uri.startsWith("/transfer")) {
      int branchId =
          Integer.parseInt(Utility.getParam(uri, Constants.BRANCH_ID));
      int accountIdOrig =
          Integer.parseInt(Utility.getParam(uri, Constants.ACCOUNT_ID_ORIG));
      int accountIdDest =
          Integer.parseInt(Utility.getParam(uri, Constants.ACCOUNT_ID_DEST));
      double amount =
          Double.parseDouble(Utility.getParam(uri, Constants.AMOUNT));
      try {
        transfer(branchId, accountIdOrig, accountIdDest, amount);
      } catch (Exception err) {
        response.setError(err);
      }
    } else if (uri.startsWith("/calculateExposure")) {
      int branchId =
          Integer.parseInt(Utility.getParam(uri, Constants.BRANCH_ID));
      try {
        double exposure = calculateExposure(branchId);
        response.setContent(Double.toString(exposure));
      } catch (Exception err) {
        response.setError(err);
      }
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

  private Branch getBranch(int branchId) throws InexistentBranchException {
    if (!branches.containsKey(branchId)) {
      throw new InexistentBranchException(branchId);
    }
    return branches.get(branchId);
  }

	public void credit(int branchId, int accountId, double amount)
	    throws InexistentBranchException, InexistentAccountException,
			       NegativeAmountException {
    if (accountId < 0) {
      throw new InexistentAccountException(accountId);
    }
    if (amount < 0) {
      throw new NegativeAmountException(amount);
    }
    getBranch(branchId).credit(accountId, amount);
  }

	public void debit(int branchId, int accountId, double amount)
	    throws InexistentBranchException, InexistentAccountException,
			       NegativeAmountException {
    if (accountId < 0) {
      throw new InexistentAccountException(accountId);
    }
    if (amount < 0) {
      throw new NegativeAmountException(amount);
    }
    getBranch(branchId).debit(accountId, amount);
   }

	public void transfer(int branchId, int accountIdOrig, int accountIdDest,
	                     double amount)
	    throws InexistentBranchException, InexistentAccountException,
			       NegativeAmountException {
     if (accountIdOrig < 0) {
       throw new InexistentAccountException(accountIdOrig);
     }
     if (accountIdDest < 0) {
       throw new InexistentAccountException(accountIdDest);
     }
     if (amount < 0) {
       throw new NegativeAmountException(amount);
     }
     getBranch(branchId).transfer(accountIdOrig, accountIdDest, amount);
   }

	public double calculateExposure(int branchId)
	    throws InexistentBranchException {
    return getBranch(branchId).calculateExposure();
  }

}
