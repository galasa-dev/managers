/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.resource.internal;

import java.util.HashMap;

import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.cicsresource.CicsJvmserverResourceException;
import dev.galasa.cicsts.cicsresource.CicsResourceManagerException;
import dev.galasa.cicsts.cicsresource.ICicsResource;
import dev.galasa.cicsts.cicsresource.IJvmprofile;
import dev.galasa.cicsts.cicsresource.IJvmserver;
import dev.galasa.cicsts.cicsresource.IJvmserver.JvmserverType;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosliberty.IZosLibertyServer;

public class CicsResourceImpl implements ICicsResource {

	private CicsResourceManagerImpl cicsResourceManagerImpl;
	private ICicsRegion cicsRegion;
	private IZosImage zosImage;
	private IZosFileHandler zosFileHandler;

	public CicsResourceImpl(CicsResourceManagerImpl cicsResourceManagerImpl, ICicsRegion cicsRegion) throws CicsResourceManagerException {
		this.cicsResourceManagerImpl = cicsResourceManagerImpl;
		this.cicsRegion = cicsRegion;
		this.zosImage = cicsRegion.getZosImage();
		try {
			this.zosFileHandler =  cicsResourceManagerImpl.getZosFileHandler();
		} catch (CicsResourceManagerException e) {
			throw new CicsResourceManagerException("Unable to get zOS File Handler", e);
		}
	}

	@Override
	public IJvmserver newJvmserver(ICicsTerminal cicsTerminal, String name, String group, String jvmprofileName, JvmserverType jvmserverType) throws CicsJvmserverResourceException {
		return new JvmserverImpl(this.cicsResourceManagerImpl, this.cicsRegion, cicsTerminal, name, group, jvmprofileName, jvmserverType);
	}

	@Override
	public IJvmserver newJvmserver(ICicsTerminal cicsTerminal, String name, String group, IJvmprofile jvmprofile) throws CicsJvmserverResourceException {
		return new JvmserverImpl(this.cicsResourceManagerImpl, this.cicsRegion, cicsTerminal, name, group, jvmprofile);
	}

	@Override
	public IJvmserver newLibertyJvmserver(ICicsTerminal cicsTerminal, String name, String group, IJvmprofile jvmprofile, IZosLibertyServer libertyServer) throws CicsJvmserverResourceException {
		return new JvmserverImpl(this.cicsResourceManagerImpl, this.cicsRegion, cicsTerminal, name, group, jvmprofile, libertyServer);
	}

	@Override
	public IJvmprofile newJvmprofile(String jvmprofileName) {
		return new JvmprofileImpl(this.zosFileHandler, this.zosImage, jvmprofileName);
	}

	@Override
	public IJvmprofile newJvmprofile(String jvmprofileName, HashMap<String, String> content) {
		return new JvmprofileImpl(this.zosFileHandler, this.zosImage, jvmprofileName, content);
	}

}
