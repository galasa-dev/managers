/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.db2.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.db2.Db2ManagerException;
import dev.galasa.db2.IDb2Schema;
import dev.galasa.db2.IResultMap;
import dev.galasa.db2.internal.properties.Db2DSESchemaName;
import dev.galasa.framework.spi.IFramework;

public class Db2SchemaImpl implements IDb2Schema{
	private Connection 		conn;
	private String 			schemaName;
	private boolean 		archive;
	private Path 			artifactRoot;
	private int 			resultSetType;
	private int 			resultSetConcurrency;
	
	private static String 	DB2_NAMESPACE = "db2";
	private static String 	DB2_STATEMENTS = "statements";
	
	private Integer 		written = 0;
	
	private static final Log  	logger = LogFactory.getLog(Db2SchemaImpl.class);
	
	
	public Db2SchemaImpl(IFramework framework,Db2InstanceImpl db2, String tag, boolean archive,
			int resultSetType, int resultSetConcurrency) throws Db2ManagerException {
		this.artifactRoot 			= framework.getResultArchiveStore().getStoredArtifactsRoot();
		this.archive 				= archive;
		this.schemaName 			= Db2DSESchemaName.get(tag);
		this.resultSetType 			= resultSetType;
		this.resultSetConcurrency 	= resultSetConcurrency;
		this.conn 					= db2.getConnection();
		
		try {
			boolean exists = false;
			PreparedStatement stmt = this.conn.prepareStatement("select schemaname from syscat.schemata");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				if (rs.getString(1).equals(this.schemaName)) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				throw new Db2ManagerException("Schema " + this.schemaName + " is not found");
			}
			
			this.conn.setSchema(this.schemaName);
		} catch (SQLException e) {
			throw new Db2ManagerException("Failed to set Schema ", e);
		}
	}

	@Override
	public String getSchemaName() {
		return this.schemaName;
		
	} 
		

	@Override
	public IResultMap executeSql(String stmt, Object... params) throws Db2ManagerException {
		ResultMap rsm = new ResultMap();
		try {
			PreparedStatement ps = createPreparedStatement(stmt, params);
			if (stmt.toUpperCase().startsWith("SELECT")) {
				ResultSet rs = ps.executeQuery();
				ResultSetMetaData meta = rs.getMetaData();
				
				rs.next();
				for (int i=1;i<=meta.getColumnCount();i++) {
					rsm.add(meta.getColumnName(i), rs.getObject(i));
				}
				ps.close();
			} else{
				int rc = ps.executeUpdate();
				rsm.add("RC", rc);
				ps.close();
			}
			
			if (archive) {
				writeCommandAndResponseToRas(stmt, rsm.valuesToString());
			}
		} catch (SQLException e) {
			throw new Db2ManagerException("Failed SQL Statement", e);
		}
		
		
		return rsm;
	}

	@Override
	public List<IResultMap> executeSqlList(String stmt, Object... params) throws Db2ManagerException {
		List<IResultMap> rsmList = new ArrayList<>();
		try {
			PreparedStatement ps = createPreparedStatement(stmt, params);
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			
			while (rs.next()) {
				ResultMap rsm = new ResultMap();
				for (int i=1;i<=meta.getColumnCount();i++) {
					rsm.add(meta.getColumnName(i), rs.getObject(i));
				}
				rsmList.add(rsm);
			}
			
			if (archive) {
				StringBuilder sb = new StringBuilder();
				for (IResultMap rm :rsmList) {
					sb.append(rm.valuesToString());
					sb.append("\n");
				}
				writeCommandAndResponseToRas(stmt, sb.toString().trim());
			}
			ps.close();
		} catch (SQLException e) {
			throw new Db2ManagerException("Failed SQL Statement", e);
		}
		return rsmList;
	}

	@Override
	public void loadCsvData(String tableName, InputStream in) throws Db2ManagerException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		try {
			String columns = br.readLine();
			String cols = createTable(tableName, columns.split(","));
			
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO "+tableName+"("+cols+") VALUES ");
			while (br.ready()) {
				String values = br.readLine();
				sb.append("(");
				sb.append(values);
				sb.append("),");
			}
			sb.deleteCharAt(sb.lastIndexOf(","));
			sb.append(";");
			
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sb.toString());
		} catch (IOException e) {
			throw new Db2ManagerException("Problem reading CSV to load table. Please ensure the CSV is line seperated with the first line being column names.", e);
		} catch (SQLException e) {
			throw new Db2ManagerException("Failed to create table.", e);
		}
		
		
		
	}
	
	private String createTable(String tablename, String[] columns) throws Db2ManagerException, SQLException {
		StringBuilder col = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		
		for (String column: columns) {
			String[] nameType = column.split(":");
			if (nameType.length != 2) {
				throw new Db2ManagerException("Column format incorrect. expecting <COLUMNNAME>;<COLUMNTYPE>, E.g Name;VARCHAR(20)");
			}
			sb.append(nameType[0] + " " + nameType[1] + ",");
			col.append(nameType[0] + ",");
		}
		
		sb.deleteCharAt(sb.lastIndexOf(","));
		col.deleteCharAt(col.lastIndexOf(","));
		sb.append(");");
		
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("CREATE TABLE " + tablename + sb.toString());
		
		return col.toString();
		
	}
	
	private void writeCommandAndResponseToRas(String stmt, String resp) {
		// Statement in
		writeToRas(stmt, artifactRoot.resolve(DB2_NAMESPACE).
				resolve(this.schemaName).
				resolve(DB2_STATEMENTS).
				resolve(written.toString() + "-" + stmt.split(" ")[0]).
				resolve("in"));
		//Response
		writeToRas(resp, artifactRoot.resolve(DB2_NAMESPACE).
				resolve(this.schemaName).
				resolve(DB2_STATEMENTS).
				resolve(written.toString() + "-" + stmt.split(" ")[0]).
				resolve("out"));
		
		written++;
		
	}
	
	private void writeToRas(String content, Path path) {
		try {
			Files.write(path, content.getBytes(), StandardOpenOption.CREATE);
		} catch (Exception e) {
			logger.info("Unable to log message for a queue", e);
		}
	}
	
	private PreparedStatement createPreparedStatement(String stmt, Object... params) throws SQLException, Db2ManagerException {
		PreparedStatement ps = conn.prepareStatement(stmt, resultSetType, resultSetConcurrency);
		for (int i=0;i<params.length;i++) {
			if (params[i] instanceof String) {
				ps.setString(i+1, (String)params[i]);
			} else if (params[i] instanceof Integer) {
				ps.setInt(i+1, (Integer)params[i]);
			} else if (params[i] instanceof Float) {
				ps.setFloat(i+1, (Float)params[i]);
			} else if (params[i] instanceof Double) {
				ps.setDouble(i+1, (Double)params[i]);
			} else if (params[i] instanceof Boolean) {
				ps.setBoolean(i+1, (Boolean)params[i]);
			} else if (params[i] instanceof byte[]) {
				ps.setBytes(i+1, (byte[])params[i]);
			} else if (params[i] instanceof BigDecimal) {
				ps.setBigDecimal(i+1, (BigDecimal)params[i]);
			}  else if (params[i] instanceof Date) {
				ps.setDate(i+1, (Date)params[i]);
			} else if (params[i] instanceof Time) {
				ps.setTime(i+1, (Time)params[i]);
			} else if (params[i] instanceof Timestamp) {
				ps.setTimestamp(i+1, (Timestamp)params[i]);
			} else if (params[i] instanceof Long) {
				ps.setLong(i+1, (Long)params[i]);
			} else if (params[i] instanceof URL) {
				ps.setURL(i+1, (URL)params[i]);
			} else {
				throw new Db2ManagerException("Galasa's excuteSQL for db2 implementation only supports basic types. Please retireve the connection from the IDb2Instance object for full java SQL capabilities");
			}
			
		}
		return ps;
	}

}
