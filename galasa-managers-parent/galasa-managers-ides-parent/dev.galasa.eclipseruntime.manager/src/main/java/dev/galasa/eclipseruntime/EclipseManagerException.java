/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.eclipseruntime;

import dev.galasa.ManagerException;

public class EclipseManagerException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public EclipseManagerException() {
    }

    public EclipseManagerException(String message) {
        super(message);
    }
    
    public EclipseManagerException(Throwable cause) {
    	super(cause);
    }
    
    public EclipseManagerException(String message, Throwable cause){
    	super(message, cause);
    }
}
