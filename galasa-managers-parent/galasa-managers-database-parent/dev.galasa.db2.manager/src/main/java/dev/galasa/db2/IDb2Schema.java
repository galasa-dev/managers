package dev.galasa.db2;

public interface IDb2Schema {
	
	// Allows statements to be applied.
	public void applyStatement(String statement);
	
	// Returns the name of this Schema
	public void getSchemaName();
	
	// Returns database name
	public void getDatabaseName();
	
}
