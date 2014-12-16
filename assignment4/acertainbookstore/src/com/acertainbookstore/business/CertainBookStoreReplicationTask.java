package com.acertainbookstore.business;

import java.util.concurrent.Callable;

import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.interfaces.Replication;

/**
 * CertainBookStoreReplicationTask performs replication to a slave server. It
 * returns the result of the replication on completion using ReplicationResult
 */
public class CertainBookStoreReplicationTask implements
		Callable<ReplicationResult> {

	private final Replication replication;
	private final ReplicationRequest request;

	public CertainBookStoreReplicationTask(Replication replication,
																				 ReplicationRequest request) {
		this.replication = replication;
		this.request = request;
	}

	@Override
	public ReplicationResult call() throws Exception {
		return replication.replicate(request);
	}

}
