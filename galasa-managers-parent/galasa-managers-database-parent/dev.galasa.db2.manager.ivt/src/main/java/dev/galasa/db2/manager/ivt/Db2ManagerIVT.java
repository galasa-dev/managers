/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.db2.manager.ivt;

import dev.galasa.Test;
import dev.galasa.db2.Db2;
import dev.galasa.db2.Db2ManagerException;
import dev.galasa.db2.Db2Schema;
import dev.galasa.db2.IDb2;
import dev.galasa.db2.IDb2Schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Test
public class Db2ManagerIVT {
	@Db2
	public IDb2 db2;
	
	@Db2Schema
	public IDb2Schema schema;
	
	@Test
	public void TestDb2NotNull() {
		assertThat(db2).isNotNull();
	}
	
	@Test
	public void TestSchemaNotNull() {
		assertThat(schema).isNotNull();
	}
	
	@Test
	public void TestConnectionNotNull() throws Db2ManagerException {
		assertThat(db2.getConnection()).isNotNull();
	}
	
	@Test
	public void TestConnection() throws SQLException, Db2ManagerException {
		Connection con = db2.getConnection();
		Statement stmt = con.createStatement(); 
		System.out.println("**** Created JDBC Statement object");

		// Execute a query and generate a ResultSet instance
		ResultSet rs = stmt.executeQuery("SELECT EMPNO FROM EMPLOYEE");
		System.out.println("**** Created JDBC ResultSet object");

		assertThat(rs.getString(1)).isNotNull();
	}

}
