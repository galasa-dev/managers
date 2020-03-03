/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch;

/**
 * <p>Represents a privovision zOS Batch Jobname</p>
 * 
 * <p>Use a {@link ZosBatchJobname} annotation to populate this field with</p>
 * 
 * @author Michael Baylis
 *
 */
public interface IZosBatchJobname {

    /**
     * Enumeration of data set types:
     * <li>{@link #SCAN}</li>
     * <li>{@link #HOLD}</li>
     * <li>{@link #JCLHOLD}</li>
     * <li>{@link #COPY}</li>
     */
    public enum TYPRUN {

        SCAN("TYPRUN=SCAN"),
        HOLD("TYPRUN=HOLD"),
        JCLHOLD("TYPRUN=JCLHOLD"),
        COPY("TYPRUN=COPY");

        private String param;
        
        TYPRUN(String param) {
            this.param = param;
        }
        
        @Override
        public String toString() {
            return param;
        }

    }
 
    /**
     * Get the name of the zOS batch Jobname
     * @return String
     */
    public String getName();

    /**
     * Set the TYPRUN property on the jobcard.
     */
    public void setTypeRun(TYPRUN type);

    /**
     * Collect all the passed parameters and pass them back in a comma seperated list
     * @return String
     */
    public String getParams();
    
}
