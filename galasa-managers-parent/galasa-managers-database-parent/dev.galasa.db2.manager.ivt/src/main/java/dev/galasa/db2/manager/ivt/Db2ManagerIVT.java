package dev.galasa.db2.manager.ivt;

import dev.galasa.Test;
import dev.galasa.db2.Db2;
import dev.galasa.db2.Db2Schema;
import dev.galasa.db2.IDb2;
import dev.galasa.db2.IDb2Schema;

import static org.assertj.core.api.Assertions.assertThat;

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
}
