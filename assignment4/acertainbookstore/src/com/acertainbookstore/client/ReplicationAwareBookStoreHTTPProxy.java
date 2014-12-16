package com.acertainbookstore.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookRating;
import com.acertainbookstore.client.ServerWorkload;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreResult;
import com.acertainbookstore.utils.BookStoreTimeoutException;
import com.acertainbookstore.utils.BookStoreUtility;

/**
 *
 * ReplicationAwareBookStoreHTTPProxy implements the client level synchronous
 * CertainBookStore API declared in the BookStore class. It keeps retrying the
 * API until a consistent reply is returned from the replicas
 *
 */
public class ReplicationAwareBookStoreHTTPProxy implements BookStore {

	private HttpClient client;
	private Set<String> slaveAddresses;
	private String masterAddress;
	private String filePath = System.getProperty("user.dir") +
	 												  "/proxy.properties";
	private volatile long snapshotId = 0;
	private ArrayList<ServerWorkload> workloads;
	private final ReentrantReadWriteLock workloadLock =
	    new ReentrantReadWriteLock(true);
	private static final int MAX_RETRIES = 5;

	public long getSnapshotId() {
		return snapshotId;
	}

	public void setSnapshotId(long snapShotId) {
		this.snapshotId = snapShotId;
	}

	/**
	 * Initialize the client object
	 */
	public ReplicationAwareBookStoreHTTPProxy() throws Exception {
		initializeReplicationAwareMappings();
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

	private void initializeReplicationAwareMappings() throws IOException {

		Properties props = new Properties();
		slaveAddresses = new HashSet<String>();

		props.load(new FileInputStream(filePath));
		this.masterAddress = props.getProperty(BookStoreConstants.KEY_MASTER);
		if (!this.masterAddress.toLowerCase().startsWith("http://")) {
			this.masterAddress = new String("http://" + this.masterAddress);
		}

		String slaveAddresses = props.getProperty(BookStoreConstants.KEY_SLAVE);
		String[] splitAddresses =
		    slaveAddresses.split(BookStoreConstants.SPLIT_SLAVE_REGEX);
		workloads = new ArrayList<ServerWorkload>(splitAddresses.length+1);
		workloads.add(new ServerWorkload(masterAddress));
		for (String slave : splitAddresses) {
			if (!slave.toLowerCase().startsWith("http://")) {
				slave = new String("http://" + slave);
			}
			this.slaveAddresses.add(slave);
			workloads.add(new ServerWorkload(slave));
		}

	}

	// public String getReplicaAddress() {
	// 	return ""; // TODO
	// }

	private ServerWorkload getLowestWorkload() {
		workloadLock.readLock().lock();
		try {
			ServerWorkload workload = Collections.min(workloads);
			workload.increment();
			return workload;
		} finally {
			workloadLock.readLock().unlock();
		}
	}

	private void returnWorkload(ServerWorkload workload) {
		workloadLock.readLock().lock();
		try {
			workload.decrement();
		} finally {
			workloadLock.readLock().unlock();
		}
	}

	private void removeServer(ServerWorkload workload) {
		workloadLock.writeLock().lock();
		try {
			workloads.remove(workload);
			if (workload.getServer() == masterAddress) {
				masterAddress = null;
			}
		} finally {
			workloadLock.writeLock().unlock();
		}
	}

	public String getMasterServerAddress() {
		return this.masterAddress;
	}

	public void buyBooks(Set<BookCopy> isbnSet) throws BookStoreException {

		String listISBNsxmlString = BookStoreUtility
				.serializeObjectToXMLString(isbnSet);
		Buffer requestContent = new ByteArrayBuffer(listISBNsxmlString);

		BookStoreResult result = null;

		ContentExchange exchange = new ContentExchange();
		String urlString = getMasterServerAddress();
		if (urlString == null) {
			throw new BookStoreException(BookStoreConstants.MASTER_DOWN);
		}
		urlString += "/" + BookStoreMessageTag.BUYBOOKS;
		exchange.setMethod("POST");
		exchange.setURL(urlString);
		exchange.setRequestContent(requestContent);
		result = BookStoreUtility.SendAndRecv(this.client, exchange);
		this.setSnapshotId(result.getSnapshotId());
	}

	@SuppressWarnings("unchecked")
	public List<Book> getBooks(Set<Integer> isbnSet) throws BookStoreException {

		String listISBNsxmlString = BookStoreUtility
				.serializeObjectToXMLString(isbnSet);
		Buffer requestContent = new ByteArrayBuffer(listISBNsxmlString);

		BookStoreResult result = null;
		int tries = 0;
		do {
			ContentExchange exchange = new ContentExchange();
			ServerWorkload workload = getLowestWorkload();
			String urlString = workload.getServer() + "/"
					+ BookStoreMessageTag.GETBOOKS;
			exchange.setMethod("POST");
			exchange.setURL(urlString);
			exchange.setRequestContent(requestContent);
			try {
				result = BookStoreUtility.SendAndRecv(this.client, exchange);
			} catch (BookStoreTimeoutException err) {
				removeServer(workload);
				continue;
			}
			returnWorkload(workload);
			if (++tries > MAX_RETRIES) {
				throw new BookStoreException(BookStoreConstants.MAX_RETRIES);
			}
		} while (result.getSnapshotId() < this.getSnapshotId());
		this.setSnapshotId(result.getSnapshotId());
		return (List<Book>) result.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Book> getEditorPicks(int numBooks) throws BookStoreException {
		ContentExchange exchange = new ContentExchange();
		String urlEncodedNumBooks = null;

		try {
			urlEncodedNumBooks = URLEncoder.encode(Integer.toString(numBooks),
					"UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new BookStoreException("unsupported encoding of numbooks", ex);
		}

		BookStoreResult result = null;
		int tries = 0;
		do {
			ServerWorkload workload = getLowestWorkload();
			String urlString = workload.getServer() + "/"
					+ BookStoreMessageTag.EDITORPICKS + "?"
					+ BookStoreConstants.BOOK_NUM_PARAM + "="
					+ urlEncodedNumBooks;
			exchange.setURL(urlString);
			try {
				result = BookStoreUtility.SendAndRecv(this.client, exchange);
			} catch (BookStoreTimeoutException err) {
				removeServer(workload);
				continue;
			}
			returnWorkload(workload);
			if (++tries > MAX_RETRIES) {
				throw new BookStoreException(BookStoreConstants.MAX_RETRIES);
			}
		} while (result.getSnapshotId() < this.getSnapshotId());
		this.setSnapshotId(result.getSnapshotId());

		return (List<Book>) result.getResultList();
	}

	public void stop() {
		try {
			client.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void rateBooks(Set<BookRating> bookRating) throws BookStoreException {
		// TODO Auto-generated method stub
		throw new BookStoreException("Not implemented");
	}

	@Override
	public List<Book> getTopRatedBooks(int numBooks) throws BookStoreException {
		// TODO Auto-generated method stub
		throw new BookStoreException("Not implemented");
	}

}
