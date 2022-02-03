/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.db2.internal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import dev.galasa.db2.Db2ManagerException;
import dev.galasa.db2.IDb2;
import dev.galasa.db2.internal.properties.Db2DSEInstance;

public class Db2Impl implements IDb2{
	Connection conn;
	
	public Db2Impl() throws Db2ManagerException{
		// Need to think how to get the url
		String db2Url = Db2DSEInstance.get("PRIMARY");
		try {
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			conn = DriverManager.getConnection(db2Url);
		} catch (ClassNotFoundException e) {
			throw new Db2ManagerException("Could not load the com.ibm.db2.jcc.DB2Driver", e);
		} catch (SQLException e) {
			throw new Db2ManagerException("Failed to connect to " + db2Url, e);
		}
	}

	@Override
	public Connection getConnection() throws Db2ManagerException {
		return this.conn;
	}

}
