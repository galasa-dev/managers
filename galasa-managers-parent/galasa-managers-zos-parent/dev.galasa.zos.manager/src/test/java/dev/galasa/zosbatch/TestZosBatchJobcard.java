/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.galasa.zosbatch.ZosBatchJobcard.Typrun;

public class TestZosBatchJobcard {

    private ZosBatchJobcard zosBatchJobcard;
    
    @Before
    public void setup() throws Exception {        
        zosBatchJobcard = new ZosBatchJobcard();
    }
    
    @Test
    public void testInputClass() {
        String value = "inputClass";
        Assert.assertEquals("problem with inputClass value", value, zosBatchJobcard.setInputClass(value).getInputClass());
    }
    
    @Test
    public void testMsgClass() {
        String value = "msgClass";
        Assert.assertEquals("problem with msgClass value", value, zosBatchJobcard.setMsgClass(value).getMsgClass());
    }
    
    @Test
    public void testmsgLevel() {
        String value = "msgLevel";
        Assert.assertEquals("problem with msgLevel value", value, zosBatchJobcard.setMsgLevel(value).getMsgLevel());
    }
    
    @Test
    public void testRegion() {
        String value = "region";
        Assert.assertEquals("problem with region value", value, zosBatchJobcard.setRegion(value).getRegion());
    }
    
    @Test
    public void testMemlimit() {
        String value = "memlimit";
        Assert.assertEquals("problem with memlimit value", value, zosBatchJobcard.setMemlimit(value).getMemlimit());
    }
    
    @Test
    public void testTyprun() {
        Typrun value = Typrun.COPY;
        Assert.assertEquals("problem with typrun value", value, zosBatchJobcard.setTyprun(value).getTyprun());
        value = Typrun.HOLD;
        Assert.assertEquals("problem with typrun value", value, zosBatchJobcard.setTyprun(value).getTyprun());
        value = Typrun.JCLHOLD;
        Assert.assertEquals("problem with typrun value", value, zosBatchJobcard.setTyprun(value).getTyprun());
        value = Typrun.SCAN;
        Assert.assertEquals("problem with typrun value", value, zosBatchJobcard.setTyprun(value).getTyprun());
    }
    
    @Test
    public void testUserid() {
        String value = "userid";
        Assert.assertEquals("problem with userid value", value, zosBatchJobcard.setUserid(value).getUserid());
    }
    
    @Test
    public void testPassword() {
        String value = "password";
        Assert.assertEquals("problem with password value", value, zosBatchJobcard.setPassword(value).getPassword());
    }
    
    @Test
    public void testCond() {
        String value = "cond";
        Assert.assertEquals("problem with cond value", value, zosBatchJobcard.setCond(value).getCond());
    }
    
    @Test
    public void testAccount() {
        String value = "account";
        Assert.assertEquals("problem with account value", value, zosBatchJobcard.setAccount(value).getAccount());
    }
    
    @Test
    public void testProgrammerName() {
        String value = "programmerName";
        Assert.assertEquals("problem with programmerName value", value, zosBatchJobcard.setProgrammerName(value).getProgrammerName());
    }
    
    @Test
    public void testNulled() {
        Assert.assertEquals("problem with nulled()", null, zosBatchJobcard.nulled(null));
        Assert.assertEquals("problem with nulled()", null, zosBatchJobcard.nulled("  "));
        Assert.assertEquals("problem with nulled()", "value", zosBatchJobcard.nulled(" value "));
        Assert.assertEquals("problem with nulled()", "value", zosBatchJobcard.nulled("value"));
    }
}
