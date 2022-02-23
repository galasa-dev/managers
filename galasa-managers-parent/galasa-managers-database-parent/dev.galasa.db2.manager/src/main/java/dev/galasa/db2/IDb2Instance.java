/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.db2;

import java.sql.Connection;

public interface IDb2Instance {
	
	public Connection getConnection();
	
	public String getDatabaseName() throws Db2ManagerException;

}
