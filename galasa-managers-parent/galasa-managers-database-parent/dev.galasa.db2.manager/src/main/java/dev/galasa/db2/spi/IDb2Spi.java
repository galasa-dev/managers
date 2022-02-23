package dev.galasa.db2.spi;

import java.sql.Connection;

import dev.galasa.db2.Db2ManagerException;

public interface IDb2Spi {
	
	public Connection getConnectionFromTag(String tag) throws Db2ManagerException;
	
}
