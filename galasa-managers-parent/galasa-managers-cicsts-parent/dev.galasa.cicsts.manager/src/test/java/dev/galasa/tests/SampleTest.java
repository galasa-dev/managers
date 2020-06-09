/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.tests;

import dev.galasa.Test;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.ICicsRegion;

@Test
public class SampleTest {

    @CicsRegion
    public ICicsRegion region1;

    @CicsRegion(cicsTag="SECONDARY")
    public ICicsRegion region2;
    
}