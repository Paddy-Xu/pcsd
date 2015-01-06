package com.acertainbank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AccountManagerHTTPProxy implements AccountManager {

	private final HttpClient client = new HttpClient();
	private final ArrayList<Address> partitions;
	private final HashMap<Integer, Integer> branches;

	public AccountManagerHTTPProxy(String configFile)
	    throws ConfigurationException {
		// Retrieve configuration of partitions and branches
		Element config;
		try {
			config = Utility.readXmlFile(configFile);
		} catch (Exception err) {
			throw new ConfigurationException(err.getMessage());
		}
		// Parse partition configuration
		NodeList partitionList = config.getElementsByTagName("Partition");
		partitions = new ArrayList<Address>(partitionList.getLength());
		for (int i = 0; i < partitionList.getLength(); ++i) {
			NamedNodeMap partition = partitionList.item(i).getAttributes();
			partitions.add(new Address(
			    partition.getNamedItem("address").getNodeValue(),
					Integer.parseInt(partition.getNamedItem("port").getNodeValue())));
		}
		// Parse branch configuration
		NodeList branchList = config.getElementsByTagName("Branch");
		branches = new HashMap<Integer, Integer>(branchList.getLength());
		for (int i = 0; i < branchList.getLength(); ++i) {
			Node branch = branchList.item(i);
			Integer partitionId = Integer.parseInt(
					branch.getAttributes().getNamedItem("partitionId").getNodeValue());
			if (partitionId < 0 || partitionId >= partitions.size()) {
				throw new ConfigurationException(
				    "Invalid partition id specified for branch:" + partitionId);
			}
			Integer branchId = Integer.parseInt(
					branch.getAttributes().getNamedItem("id").getNodeValue());
			if (branchId < 0) {
				throw new ConfigurationException(
				    "Invalid branch id specified for branch" + branchId);
			}
			branches.put(branchId, partitionId);
		}
		// Set up HTTP client
		client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
		client.setMaxConnectionsPerAddress(
		    Integer.parseInt(config.getAttribute("maxConnectionsPerAddress")));
		// TODO: when specifying a max thread pool size, client.start() never
		//       returns.
		// client.setThreadPool(new QueuedThreadPool(
		//     Integer.parseInt(config.getAttribute("threadPool"))));
		client.setTimeout(
		    Integer.parseInt(config.getAttribute("timeout")));
		try {
			client.start();
		} catch (Exception err) {
			throw new ConfigurationException(err.getMessage());
		}
	}

	public void credit(int branchId, int accountId, double amount)
	    throws InexistentBranchException, InexistentAccountException,
			       NegativeAmountException {
    if (!branches.containsKey(branchId)) {
			throw new InexistentBranchException(branchId);
		}
		if (amount < 0) {
			throw new NegativeAmountException(amount);
		}
		ContentExchange exchange = new ContentExchange();
		exchange.setMethod("POST");
		exchange.setAddress(partitions.get(branches.get(branchId)));
		exchange.setRequestURI(
		    "/" + Constants.CREDIT +
				"?" + Constants.BRANCH_ID + "=" + Integer.toString(branchId) +
				"&" + Constants.ACCOUNT_ID + "=" + Integer.toString(accountId) +
				"&" + Constants.AMOUNT + "=" + Double.toString(amount));
		Response response = null;
		try {
			response = Utility.rpc(client, exchange);
		} catch (CommunicationException err) {
			throw new InexistentBranchException("Communication with branch failed",
																					branchId);
		}
		if (!response.wasSuccessful()) {
			Exception error = response.getError();
			if (error instanceof InexistentAccountException) {
				throw (InexistentAccountException)error;
			}
			if (error instanceof InexistentBranchException) {
				throw (InexistentBranchException)error;
			}
			if (error instanceof NegativeAmountException) {
				throw (NegativeAmountException)error;
			}
			if (error instanceof CommunicationException) {
				// Cannot throw these :-(
			}
			// Else the error is unknown...
		}
  }

	public void debit(int branchId, int accountId, double amount)
	    throws InexistentBranchException, InexistentAccountException,
			       NegativeAmountException {
    if (!branches.containsKey(branchId)) {
			throw new InexistentBranchException(branchId);
		}
		if (amount < 0) {
			throw new NegativeAmountException(amount);
		}
		ContentExchange exchange = new ContentExchange();
		exchange.setMethod("POST");
		exchange.setAddress(partitions.get(branches.get(branchId)));
		exchange.setRequestURI(
		    "/" + Constants.DEBIT +
				"?" + Constants.BRANCH_ID + "=" + Integer.toString(branchId) +
				"&" + Constants.ACCOUNT_ID + "=" + Integer.toString(accountId) +
				"&" + Constants.AMOUNT + "=" + Double.toString(amount));
		Response response = null;
		try {
			response = Utility.rpc(client, exchange);
		} catch (CommunicationException err) {
			throw new InexistentBranchException("Communication with branch failed",
																					branchId);
		}
		if (!response.wasSuccessful()) {
			Exception error = response.getError();
			if (error instanceof InexistentAccountException) {
				throw (InexistentAccountException)error;
			}
			if (error instanceof InexistentBranchException) {
				throw (InexistentBranchException)error;
			}
			if (error instanceof NegativeAmountException) {
				throw (NegativeAmountException)error;
			}
			if (error instanceof CommunicationException) {
				// Cannot throw these :-(
			}
			// Else the error is unknown...
		}
   }

	public void transfer(int branchId, int accountIdOrig, int accountIdDest,
	                     double amount)
	    throws InexistentBranchException, InexistentAccountException,
			       NegativeAmountException {
    if (!branches.containsKey(branchId)) {
			throw new InexistentBranchException(branchId);
		}
		if (accountIdOrig < 0) {
			throw new InexistentAccountException(accountIdOrig);
		}
		if (accountIdDest < 0) {
			throw new InexistentAccountException(accountIdDest);
		}
		if (amount < 0) {
			throw new NegativeAmountException(amount);
		}
		ContentExchange exchange = new ContentExchange();
		exchange.setMethod("POST");
		exchange.setAddress(partitions.get(branches.get(branchId)));
		exchange.setRequestURI(
		    "/" + Constants.TRANSFER +
				"?" + Constants.BRANCH_ID + "=" + Integer.toString(branchId) +
				"&" + Constants.ACCOUNT_ID_ORIG + "=" +
				      Integer.toString(accountIdOrig) +
				"&" + Constants.ACCOUNT_ID_DEST + "=" +
				      Integer.toString(accountIdDest) +
				"&" + Constants.AMOUNT + "=" + Double.toString(amount));
		Response response = null;
		try {
			response = Utility.rpc(client, exchange);
		} catch (CommunicationException err) {
			throw new InexistentBranchException("Communication with branch failed",
																					branchId);
		}
		if (!response.wasSuccessful()) {
			Exception error = response.getError();
			if (error instanceof InexistentAccountException) {
				throw (InexistentAccountException)error;
			}
			if (error instanceof InexistentBranchException) {
				throw (InexistentBranchException)error;
			}
			if (error instanceof NegativeAmountException) {
				throw (NegativeAmountException)error;
			}
			if (error instanceof CommunicationException) {
				// Cannot throw these :-(
			}
			// Else the error is unknown...
		}
  }

	public double calculateExposure(int branchId)
	    throws InexistentBranchException {
    if (!branches.containsKey(branchId)) {
			throw new InexistentBranchException(branchId);
		}
		ContentExchange exchange = new ContentExchange();
		exchange.setMethod("POST");
		exchange.setAddress(partitions.get(branches.get(branchId)));
		exchange.setRequestURI(
		    "/" + Constants.CALCULATE_EXPOSURE +
				"?" + Constants.BRANCH_ID + "=" + Integer.toString(branchId));
		Response response = null;
		try {
			response = Utility.rpc(client, exchange);
		} catch (CommunicationException err) {
			throw new InexistentBranchException("Communication with branch failed",
																					branchId);
		}
		if (!response.wasSuccessful()) {
			Exception error = response.getError();
			if (error instanceof InexistentBranchException) {
				throw (InexistentBranchException)error;
			}
			if (error instanceof CommunicationException) {
				// Cannot throw these :-(
			}
			// Else the error is unknown...
		}
		return Double.parseDouble(response.getContent());
  }

}
