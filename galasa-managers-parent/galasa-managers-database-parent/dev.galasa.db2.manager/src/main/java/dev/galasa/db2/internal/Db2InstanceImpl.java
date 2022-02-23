/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.db2.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.db2.Db2ManagerException;
import dev.galasa.db2.IDb2Instance;
import dev.galasa.db2.internal.properties.Db2Credentials;
import dev.galasa.db2.internal.properties.Db2DSEInstanceName;
import dev.galasa.db2.internal.properties.Db2InstanceUrl;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;

public class Db2InstanceImpl implements IDb2Instance{
	private Connection 			conn;
	
	private static final Log  	logger = LogFactory.getLog(Db2InstanceImpl.class);
	
	public Db2InstanceImpl(IFramework framework, Db2ManagerImpl manager, String tag) throws Db2ManagerException{
		String instance = Db2DSEInstanceName.get(tag);
		
		try {
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			
			ICredentialsUsernamePassword creds = (ICredentialsUsernamePassword)framework.getCredentialsService().getCredentials(Db2Credentials.get(instance));
			String url = Db2InstanceUrl.get(instance);
			
			conn = DriverManager.getConnection(url, creds.getUsername(), creds.getPassword());
		} catch (ClassNotFoundException e) {
			throw new Db2ManagerException("Could not load the com.ibm.db2.jcc.DB2Driver", e);
		} catch (SQLException e) {
			throw new Db2ManagerException("Failed to connect to " + instance, e);
		} catch (CredentialsException e) {
			throw new Db2ManagerException("Failed to find an Credentials for: " + instance, e);
		}
	}

	@Override
	public String getDatabaseName() throws Db2ManagerException {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("VALUES CURRENT SERVER");
			rs.next();
			return rs.getString(1);
		} catch (SQLException e) {
			throw new Db2ManagerException("Failed to retrieve database name", e);
		}
		
	}
	
	public Connection getConnection() {
		return this.conn;
	}
	
	
}
