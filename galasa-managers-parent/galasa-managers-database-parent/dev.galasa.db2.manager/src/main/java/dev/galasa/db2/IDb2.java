/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.db2;

import java.sql.Connection;

public interface IDb2 {
	// Returns a standard java.sql.Connection to the Db2
	public Connection getConnection() throws Db2ManagerException;
}
