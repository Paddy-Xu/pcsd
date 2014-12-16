package com.acertainbookstore.business;

import java.util.concurrent.Callable;

import com.acertainbookstore.client.ReplicationHTTPProxy;
import com.acertainbookstore.business.ReplicationRequest;

/**
 * CertainBookStoreReplicationTask performs replication to a slave server. It
 * returns the result of the replication on completion using ReplicationResult
 */
public class CertainBookStoreReplicationTask implements
		Callable<ReplicationResult> {

	private final ReplicationHTTPProxy proxy;
	private final String server;
	private final ReplicationRequest request;

	public CertainBookStoreReplicationTask(ReplicationHTTPProxy proxy,
	  																		 String server,
																				 ReplicationRequest request) {
		this.proxy = proxy;
		this.server = server;
		this.request = request;
	}

	@Override
	public ReplicationResult call() throws Exception {
		return proxy.replicate(server, request);
	}

}
