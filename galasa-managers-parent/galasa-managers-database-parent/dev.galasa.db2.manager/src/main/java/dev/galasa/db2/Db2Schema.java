package dev.galasa.db2;

public @interface Db2Schema {
	
	String tag() default "PRIMARY";
	
	String schemaName() default "PRIMARY";
}
