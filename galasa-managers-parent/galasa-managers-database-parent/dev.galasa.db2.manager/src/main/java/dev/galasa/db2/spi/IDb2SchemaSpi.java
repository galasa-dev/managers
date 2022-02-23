package dev.galasa.db2.spi;

import dev.galasa.db2.Db2ManagerException;
import dev.galasa.db2.IDb2Schema;

public interface IDb2SchemaSpi {
	public IDb2Schema getConnectionFromTag(String tag) throws Db2ManagerException;
}
