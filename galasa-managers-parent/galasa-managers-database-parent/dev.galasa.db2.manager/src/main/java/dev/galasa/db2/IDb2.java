package dev.galasa.db2;

import java.sql.Connection;

public interface IDb2 {
	public Connection getConnection() throws Db2ManagerException;
}
