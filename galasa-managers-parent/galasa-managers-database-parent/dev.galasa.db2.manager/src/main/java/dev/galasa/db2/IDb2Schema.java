/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.db2;

import java.io.InputStream;
import java.util.List;

public interface IDb2Schema {
	
	public IResultMap executeSql(String stmt, Object... params) throws Db2ManagerException;
	
	public List<IResultMap> executeSqlList(String stmt, Object... params) throws Db2ManagerException;
	
	public void loadCsvData(String tableName, InputStream in) throws Db2ManagerException;
	
	public String getSchemaName();
	
}
