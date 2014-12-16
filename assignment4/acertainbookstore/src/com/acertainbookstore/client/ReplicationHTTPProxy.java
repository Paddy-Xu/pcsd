package com.acertainbookstore.client;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.business.ReplicationResult;
import com.acertainbookstore.interfaces.Replication;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreResponse;
import com.acertainbookstore.utils.BookStoreUtility;

public class ReplicationHTTPProxy implements Replication {

  private final String server;
	private final HttpClient client;

	/**
	 * Initialize the client object
	 */
	public ReplicationHTTPProxy(String server) throws Exception {
		String urlEnding = "/" + BookStoreMessageTag.REPLICATE;
		if (!server.endsWith(urlEnding)) {
			server += urlEnding;
		}
		this.server = server;
		client = new HttpClient();
		client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
		client.setMaxConnectionsPerAddress(
		    // max concurrent connections to every address
		    BookStoreClientConstants.CLIENT_MAX_CONNECTION_ADDRESS);
		client.setThreadPool(new QueuedThreadPool(
		    // max threads
				BookStoreClientConstants.CLIENT_MAX_THREADSPOOL_THREADS));
    // seconds timeout if no server reply, the request expires
		client.setTimeout(BookStoreClientConstants.CLIENT_MAX_TIMEOUT_MILLISECS);
		client.start();
	}

  public ReplicationResult replicate(ReplicationRequest request) {
		String serialized = BookStoreUtility.serializeObjectToXMLString(request);
		ByteArrayBuffer requestContent = new ByteArrayBuffer(serialized);
		ContentExchange exchange = new ContentExchange();
		exchange.setMethod("POST");
		exchange.setURL(server);
		exchange.setRequestContent(requestContent);
		try {
			BookStoreUtility.SendAndRecv(client, exchange);
			return new ReplicationResult(server, true);
		} catch (Exception err) {
			return new ReplicationResult(server, false);
		}
  }

}
