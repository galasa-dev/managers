/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zossecurity.internal.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zossecurity.IZosGroup;
import dev.galasa.zossecurity.IZosUserid;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.internal.ZosSecurityImpl;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.HttpMethod;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.ResourceType;
import dev.galasa.zossecurity.internal.properties.UseridBypassPassword;
import dev.galasa.zossecurity.internal.properties.UseridDefaultPassword;
import dev.galasa.zossecurity.internal.resources.RacfOutputProcessing.COMMAND;

public class ZosUseridImpl implements IZosUserid {
	
	private static final Log logger = LogFactory.getLog(ZosUseridImpl.class);

	private final ZosSecurityImpl zosSecurity;
	private final String userid;
	private String password;
	private String passphrase;
	private final IZosImage image;
	private final String sysplexId;
	private final String runName;

	private final ArrayList<ZosGroupImpl> groups = new ArrayList<ZosGroupImpl>();

	private final Map<String, String> zosSecurityServerQueryParams = new HashMap<String, String>();

	public ZosUseridImpl(ZosSecurityImpl zosSecurity, String userid, String password, String passphrase, IZosImage image) {
		this.zosSecurity = zosSecurity;
		this.userid = userid;
		this.password = password;
		this.passphrase = passphrase;
		this.image = image;
		this.sysplexId = image.getSysplexID();
		this.runName = zosSecurity.getRunName();

		zosSecurityServerQueryParams.put("runid", zosSecurity.getRunName());
	}

	public ZosUseridImpl(ZosSecurityImpl zosSecurity, String userid, String sysplexId, String runName) {
		this.zosSecurity = zosSecurity;
		this.userid = userid;
		this.password = null;
		this.passphrase = null;
		this.image = null;
		this.sysplexId = sysplexId;
		this.runName = runName;

		zosSecurityServerQueryParams.put("runid", this.runName);
	}

	@Override
	public void free() throws ZosSecurityManagerException {
		zosSecurity.dssFree(ResourceType.ZOS_USERID.getName(), getUserid());
		logger.debug("zOS Profile '" + getUserid() + "' was freed");
	}

	@Override
	public String getUserid() {
		return this.userid;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getPassphrase() {
		return this.passphrase;
	}
	
	@Override
	public IZosImage getZosImage() {
		return this.image;
	}

	@Override
	public void setPassword(String password, String passphrase) throws ZosSecurityManagerException {
		setPassword(password, passphrase, false);
	}

	@Override
	public void setPassword(String password, String passphrase, boolean expire) throws ZosSecurityManagerException {
		if (password == null || password.trim().isEmpty()) {
			throw new ZosSecurityManagerException("A password must always be provided");
		}
		
		password = password.trim();
		if (passphrase != null) {
			passphrase = passphrase.trim();
			if (passphrase.isEmpty()) {
				passphrase = null;
			}
		}
		
		this.password = password;
		this.passphrase = passphrase;

		//*** we need to do the password change in 2 steps to cope with the passphrase
		
		StringBuilder command1 = new StringBuilder();
		command1.append("PASSWORD(");
		command1.append(password);
		command1.append(")");
		
		StringBuilder command2 = new StringBuilder();
		command2.append("PASSWORD(");
		command2.append(password);
		command2.append(") ");
		
		if (passphrase == null) {
			command2.append("NOPHRASE ");
		} else {
			command2.append("PHRASE('");
			command2.append(passphrase);
			command2.append("') ");
		}
		
		if (expire) {
			command2.append("EXPIRED ");
		} else {
			command2.append("NOEXPIRED ");
		}
		
		try {
			//*** First reset the password alone
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", command1.toString());
			
			JsonObject response = zosSecurity.clientRequest(this.sysplexId, HttpMethod.PUT, "/api/userid/" + this.userid, zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.ALTUSER, getUserid(), zosSecurity.isOutputReporting());

			//*** second reset the password with no passphrase
			jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", command2.toString());

			response = zosSecurity.clientRequest(this.sysplexId, HttpMethod.PUT, "/api/userid/" + this.userid, zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.ALTUSER, getUserid(), zosSecurity.isOutputReporting());
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("ALTUSER of " + this.userid + " failed", e);
		}
	}

	protected void setKerbname(String kerbname) throws ZosSecurityManagerException {
		if (kerbname != null && kerbname.trim().isEmpty()) {
			kerbname = null;
		}
		if (kerbname != null) {
			kerbname = kerbname.trim();
		}
		
		StringBuilder command1 = new StringBuilder();
		command1.append("KERB(");
		if (kerbname != null) {
			command1.append("KERBNAME('");
			command1.append(kerbname);
			command1.append("')");
		} else { 
			command1.append("NOKERBNAME");
		}
		command1.append(") PASSWORD(");
		command1.append(password);
		command1.append(") NOEXPIRE");
		
		try {
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", command1.toString());

			JsonObject response = zosSecurity.clientRequest(this.sysplexId, HttpMethod.PUT, "/api/userid/" + this.userid, zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.ALTUSER, getUserid(), zosSecurity.isOutputReporting());

			if (zosSecurity.isResourceReporting()) {
				String listUser = listUser();
				if (!zosSecurity.isOutputReporting()) {
					logger.debug("Updated LISTUSER of '" + this.userid + "' \n" + listUser);
				}
			}
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("ALTUSER of " + this.userid + " failed", e);
		}
	}

	@Override
	public void revoke() throws ZosSecurityManagerException {
		try {
			//*** First reset the password alone
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", "REVOKE");

			JsonObject response = zosSecurity.clientRequest(this.sysplexId, HttpMethod.PUT, "/api/userid/" + this.userid, zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.ALTUSER, getUserid(), zosSecurity.isOutputReporting());
			
			if (zosSecurity.isResourceReporting()) {
				String listUser = listUser();
				if (!zosSecurity.isOutputReporting()) {
					logger.debug("Updated LISTUSER of '" + this.userid + "' \n" + listUser);
				}
			}
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("ALTUSER of " + this.userid + " failed", e);
		}
	}

	@Override
	public void resume() throws ZosSecurityManagerException {
		try {
			//*** First reset the password alone
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", "RESUME");

			JsonObject response = zosSecurity.clientRequest(this.sysplexId, HttpMethod.PUT, "/api/userid/" + this.userid, zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.ALTUSER, getUserid(), zosSecurity.isOutputReporting());
			
			if (zosSecurity.isResourceReporting()) {
				String listUser = listUser();
				if (!zosSecurity.isOutputReporting()) {
					logger.debug("Updated LISTUSER of '" + this.userid + "' \n" + listUser);
				}
			}
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("ALTUSER of " + this.userid + " failed", e);
		}
	}

	@Override
	public void connectToGroup(String groupid) throws ZosSecurityManagerException {
		try {
			JsonObject jsonBody = new JsonObject();

			JsonObject response = zosSecurity.clientRequest(this.sysplexId, HttpMethod.PUT, "/api/userid/" + this.userid + "/group/" + groupid, zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.CONNECT, getUserid(), zosSecurity.isOutputReporting());
			
			if (zosSecurity.isResourceReporting()) {
				String listUser = listUser();
				if (!zosSecurity.isOutputReporting()) {
					logger.debug("Updated LISTUSER of '" + this.userid + "' \n" + listUser);
				}
			}
			
			boolean found = false;
			for(ZosGroupImpl oldGroup : groups) {
				if (oldGroup.getGroupid().equalsIgnoreCase(groupid)) {
					found = true;
					break;
				}
			}
			if (!found) {
				ZosGroupImpl newGroup = new ZosGroupImpl(groupid);
				groups.add(newGroup);
				Collections.sort(groups);
			}
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("CONNECT of " + this.userid + " failed", e);
		}
	}

	@Override
	public void removeFromGroup(String groupid) throws ZosSecurityManagerException {
		try {
			JsonObject response = zosSecurity.clientRequest(this.sysplexId, HttpMethod.DELETE, "/api/userid/" + this.userid + "/group/" + groupid, zosSecurityServerQueryParams, null);
			RacfOutputProcessing.analyseOutput(response, COMMAND.REMOVE, getUserid(), zosSecurity.isOutputReporting());
			
			if (zosSecurity.isResourceReporting()) {
				String listUser = listUser();
				if (!zosSecurity.isOutputReporting()) {
					logger.debug("Updated LISTUSER of '" + this.userid + "' \n" + listUser);
				}
			}

			for(ZosGroupImpl oldGroup : groups) {
				if (oldGroup.getGroupid().equalsIgnoreCase(groupid)) {
					groups.remove(oldGroup);
					break;
				}
			}
		} catch (ZosSecurityManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("CONNECT of " + this.userid + " failed", e);
		}
	}

	@Override
	public IZosGroup[] getConnectedGroups() {
		return groups.toArray(new IZosGroup[groups.size()]);
	}

	@Override
	public void setWhen(String days, String time) throws ZosSecurityManagerException {
		if (days != null) {
			days = days.trim();
		}
		
		if (time != null) {
			time = time.trim();
		}		
		
		StringBuilder command1 = new StringBuilder();
		command1.append("WHEN(");
		if (days != null) {
			command1.append(" DAYS(");
			command1.append(days);
			command1.append(")");
		}
		if (time != null) {
			command1.append(" TIME(");
			command1.append(time);
			command1.append(")");
		}
		command1.append(")");
		
		try {
			//*** First reset the password alone
			JsonObject jsonBody = new JsonObject();
			jsonBody.addProperty("parameters", command1.toString());

			JsonObject response = zosSecurity.clientRequest(this.sysplexId, HttpMethod.PUT, "/api/userid/" + this.userid, zosSecurityServerQueryParams, jsonBody);
			RacfOutputProcessing.analyseOutput(response, COMMAND.ALTUSER, getUserid(), zosSecurity.isOutputReporting());

			if (zosSecurity.isResourceReporting()) {
				String listUser = listUser();
				if (!zosSecurity.isOutputReporting()) {
					logger.debug("Updated LISTUSER of '" + this.userid + "' \n" + listUser);
				}
			}	
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("ALTUSER of " + this.userid + " failed", e);
		}
	}

	public String listUser() throws ZosSecurityManagerException {
		try {
			HashMap<String, String> luQueryParams = new HashMap<String, String>(zosSecurityServerQueryParams);
			luQueryParams.put("omvs", "true");
			luQueryParams.put("kerb", "true");
			
			JsonObject response = zosSecurity.clientRequest(this.sysplexId, HttpMethod.GET, "/api/userid/" + this.userid, luQueryParams, null);
			JsonObject jsonResponse = RacfOutputProcessing.analyseOutput(response, COMMAND.LISTUSER, getUserid(), zosSecurity.isOutputReporting());
			return jsonResponse.get("output").getAsString();	
		} catch (ZosSecurityManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("LISTUSER of " + this.userid + " failed", e);
		}
	}
	
	private List<String> parseConnectedGroups(String listUser) {
		
		Pattern group = Pattern.compile("[^-]GROUP=(\\S*)");
		Matcher matchGroup = group.matcher(listUser);

		ArrayList<String> connectedGroups = new ArrayList<String>();

		while (matchGroup.find()) {
			connectedGroups.add(matchGroup.group(1));
		}

		return connectedGroups;
	}

	@Override
	public String toString() {
		return "[zOS Security Userid] " + userid;
	}
	
	public static ZosUseridImpl allocateUserId(ZosSecurityImpl zosSecurity) throws ZosSecurityManagerException {
//	TODO - see also ZosCicsClassSetImpl#allocateClassset(ZosSecurityImpl zosSecurity)
//		Random random = new Random();
//
//		int minimumFree = 0;
//		if (preallocate) {
//			minimumFree = CicsClassSetMinimumFree.get(zosSecurity.getZosImage());
//		}
		String userName = zosSecurity.getUseridFromPool();
		String defaultPassword = UseridDefaultPassword.get();
		boolean bypassPassword = UseridBypassPassword.get();
		
		ZosUseridImpl userid = new ZosUseridImpl(zosSecurity, userName, defaultPassword, null, zosSecurity.getZosImage());

		logger.debug("zOS Userid '" + userid.getUserid() + "' was allocated to this run");

		// change password to the default one unless bypassed
		if (!bypassPassword) {
			userid.setPassword(defaultPassword, null, false);
		}
		
		String listUser = userid.listUser();
		List<String> connectedGroups = userid.parseConnectedGroups(listUser);
		for(String connectedgroup : connectedGroups) {
			userid.groups.add(new ZosGroupImpl(connectedgroup));
		}
		Collections.sort(userid.groups);

		if (zosSecurity.isResourceReporting() && !zosSecurity.isOutputReporting()) {
			logger.debug("LISTUSER of '" + userid.getUserid() + "' \n" + listUser);
		}

		return userid;
	}

	@Override
	public void delete() throws ZosSecurityManagerException {
		try {
			this.zosSecurity.dssUnregister(ResourceType.ZOS_USERID.getName(), getUserid(), sysplexId, runName);
		} catch (ZosSecurityManagerException e) {
			throw e;
//		} catch (Exception e) {
//			throw new ZosSecurityManagerException("RDELETE of " + getUserid() + " failed", e);
		}
	}
}
