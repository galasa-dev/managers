/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019,2021.
 */
package dev.galasa.linux.spi;

import dev.galasa.linux.ILinuxImage;

public interface ILinuxProvisionedImage extends ILinuxImage {
    
    void discard();

}
