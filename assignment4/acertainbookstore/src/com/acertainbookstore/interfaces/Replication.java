package com.acertainbookstore.interfaces;

import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.business.ReplicationResult;

public interface Replication {

  public ReplicationResult replicate(ReplicationRequest request);

}
