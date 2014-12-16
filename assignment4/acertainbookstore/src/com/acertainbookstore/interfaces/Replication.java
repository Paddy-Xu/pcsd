package com.acertainbookstore.interfaces;

import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreResult;

public interface Replication {

  public void replicate(ReplicationRequest request) throws BookStoreException;

}
