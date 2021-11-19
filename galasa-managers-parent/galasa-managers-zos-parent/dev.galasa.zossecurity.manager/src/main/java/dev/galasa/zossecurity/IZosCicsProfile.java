/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zossecurity;

import dev.galasa.zossecurity.datatypes.ZosCicsClassResource;
import dev.galasa.zossecurity.datatypes.ZosCicsClassType;

public interface IZosCicsProfile extends IZosProfile {
	
	/**
	 * Retrieve the CICS Security Class type
	 * 
	 * @return The CICS Security Class type
	 */
	public ZosCicsClassResource getClassResource();
	
	/**
	 * Retrieve the Member/Grouping setting
	 * 
	 * @return Member or Grouping
	 */
	public ZosCicsClassType getClassType();
	
}
