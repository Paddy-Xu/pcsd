package com.acertainbank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AccountManagerHTTPProxy {

	private final ArrayList<String> partitions;
	private final HashMap<Integer, Integer> branches;

	public AccountManagerHTTPProxy(String configFile)
	    throws ConfigurationException, IOException, ParserConfigurationException,
			       SAXException {
		// Retrieve configuration of partitions and branches
		Element config = Utility.readXmlFile(configFile);
		// Parse partition configuration
		NodeList partitionList =
		    config.getElementsByTagName("Partitions").item(0).getChildNodes();
		partitions = new ArrayList<String>(partitionList.getLength());
		for (int i = 0; i < partitionList.getLength(); ++i) {
			partitions.add(partitionList.item(i).getNodeValue());
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
			branches.put(i, partitionId);
		}
	}

	public void credit(int branchId, int accountId, double amount)
	    throws InexistentBranchException, InexistentAccountException,
			       NegativeAmountException {
     // NYI
   }

	public void debit(int branchId, int accountId, double amount)
	    throws InexistentBranchException, InexistentAccountException,
			       NegativeAmountException {
     // NYI
   }

	public void transfer(int branchId, int accountIdOrig, int accountIdDest,
	                     double amount)
	    throws InexistentBranchException, InexistentAccountException,
			       NegativeAmountException {
     // NYI
   }

	public double calculateExposure(int branchId)
	    throws InexistentBranchException {
    // NYI
		return 0f;
  }

}
