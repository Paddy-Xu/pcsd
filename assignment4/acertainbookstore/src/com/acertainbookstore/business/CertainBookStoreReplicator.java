package com.acertainbookstore.business;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.client.ReplicationHTTPProxy;
import com.acertainbookstore.interfaces.Replicator;

/**
 * CertainBookStoreReplicator is used to replicate updates to slaves
 * concurrently.
 */
public class CertainBookStoreReplicator implements Replicator {

	private final ExecutorService threadPool;
	private final HashMap<String, ReplicationHTTPProxy> proxies;

	public CertainBookStoreReplicator(Set<String> servers,
	 															    int maxReplicatorThreads) throws Exception {
		threadPool = Executors.newFixedThreadPool(maxReplicatorThreads);
		this.proxies = new HashMap<String, ReplicationHTTPProxy>(servers.size());
		for (String server : servers) {
			proxies.put(server, new ReplicationHTTPProxy(server));
		}
	}

	public List<Future<ReplicationResult>> replicate(Set<String> slaveServers,
			                                             ReplicationRequest request) {
		ArrayList<Future<ReplicationResult>> futures =
		    new ArrayList<Future<ReplicationResult>>(slaveServers.size());
		for (String server : slaveServers) {
			futures.add(threadPool.submit(
			    new CertainBookStoreReplicationTask(proxies.get(server), request)));
		}
		return futures;
	}

}
