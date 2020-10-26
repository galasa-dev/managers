package dev.galasa.cicsts.ceda.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.logging.Log;

import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.ceda.CEDA;
import dev.galasa.cicsts.ceda.CEDAException;
import dev.galasa.cicsts.ceda.ICEDA;
import dev.galasa.core.manager.Logger;
import dev.galasa.zos3270.Zos3270Exception;

@Test
public class CEDAManagerIVT {

    @CicsRegion()
    public ICicsRegion cics;

    @CicsTerminal()
    public ICicsTerminal cedaTerminal;

    @CicsTerminal()
    public ICicsTerminal cebrTerminal;

    @CicsTerminal()
    public ICicsTerminal lowerCase;

    @CEDA
    public ICEDA ceda;

    @Logger
    public Log logger;

    @BeforeClass
    public void login()throws InterruptedException, Zos3270Exception {
        //Logon to the CICS Region
        cedaTerminal.clear();
        cedaTerminal.waitForKeyboard();
        cedaTerminal.type("CEDA").enter().waitForKeyboard();

        cebrTerminal.clear();
        cebrTerminal.waitForKeyboard();

        lowerCase.clear();
        lowerCase.waitForKeyboard();
        lowerCase.type("CEOT TRANID").enter().waitForKeyboard().pf3().waitForKeyboard().clear().waitForKeyboard();
        lowerCase.type("CEDA").enter().waitForKeyboard();
    }
    @Test
    public void checkCECINotNull() {
        assertThat(ceda).isNotNull();
        assertThat(cics).isNotNull();
        assertThat(cebrTerminal).isNotNull();
        assertThat(cedaTerminal).isNotNull();
    }
    @Test
    public void testCreateResource() throws CEDAException{
        String resourceType = "STRING";
        String resourceName = "ABCDEF";
        String groupName = "ABC";
        String resourceParameters = "123";

        ceda.createResource(cedaTerminal, resourceType, resourceName, groupName, resourceParameters);
    }

    @Test
    public void testInstallGroup()throws CEDAException{
        String groupName = "ABC";
        ceda.installGroup(cedaTerminal, groupName);
    }

    @Test
    public void testInstallResource() throws CEDAException{

        String resourceType = "STRING";
        String resourceName = "ABCDEF";
        String cedaGroup = "Group 1";

        ceda.installResource(cedaTerminal, resourceType, resourceName, cedaGroup);
    }

    @Test
    public void testDeleteGroup() throws CEDAException{
        String groupName = "ABC";

        ceda.deleteGroup(cedaTerminal, groupName);
    }

    @Test
    public void testDeleteResource() throws CEDAException{
        String resourceType = "STRING";
        String resourceName = "ABCDEF";
        String groupName = "ABC";

        ceda.deleteResource(cedaTerminal, resourceType, resourceName, groupName);
    }

}
