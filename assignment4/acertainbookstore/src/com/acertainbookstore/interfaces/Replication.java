package com.acertainbookstore.interfaces;

import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.business.ReplicationResult;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreResult;

public interface Replication {

  public ReplicationResult replicate(ReplicationRequest request);

}
