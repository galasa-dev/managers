package dev.galasa.db2;

public interface IDb2Schema {
	
	// Do we want a selection of defined functions offered like this, which allows us to easily substitute in schema name and database 
	public void CreateTable();
	public void DropTable();
	//... etc
	
	//OR do we want a apply sql statement for the schema, where we expect user to know the schema name and database name:
	public void applyStatement(String statement);
	// To aid with the above
	public void getSchemaName();
	public void getDatabaseName();
	
	// Other option could be to provide variables that can be subbed in by the manager e.g &{SCHEMANAME}
}
