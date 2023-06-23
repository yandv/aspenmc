package br.com.aspenmc.backend;

public interface Database {

	/**
	 * Create a new connection to the database
	 *
	 * @throws Exception if the connection fails
	 */
	
	void createConnection() throws Exception;

	/**
	 * Close the connection to the database
	 *
	 */

	void closeConnection();

	/**
	 * Check if the connection is still open
	 *
	 * @return true if the connection is still open
	 */

	boolean isConnected();
	
}
