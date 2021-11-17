/*
* Copyright contributors to the Galasa project 
*/

package dev.galasa.eclipseruntime;

public enum EclipseVersion {
	VPHOTON("photon"),
	V201809("2018-09"),
	V201812("2018-12"),
	V201903("2019-03"),
	V201906("2019-06"),
	V201909("2019-09"),
	V201912("2019-12"),
	V202003("2020-03"),
	V202006("2020-06"),
	V202009("2020-09"),
	V202012("2020-12"),
	V202103("2021-03"),
	V202106("2021-06"),
	V202109("2021-09");
	
	private String friendlyString;
	
	private EclipseVersion(String friendlyString)
	{
		this.friendlyString=friendlyString;
	}
	
	public String getFriendlyString()
	{
		return friendlyString;
	}
}

