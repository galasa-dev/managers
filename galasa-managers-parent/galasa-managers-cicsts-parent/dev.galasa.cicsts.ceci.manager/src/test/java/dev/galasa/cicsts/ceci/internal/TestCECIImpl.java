package dev.galasa.cicsts.ceci.internal;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import dev.galasa.cicsts.ceci.CECIException;
import dev.galasa.cicsts.ceci.ICECIResponse;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.ITerminal;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;

@RunWith(PowerMockRunner.class)
public class TestCECIImpl {
    

    private CECIImpl ceci;
    
    private CECIImpl ceciSpy;
    
    // Static fields in CECIImpl
    private static final String COMMAND_EXECUTION_COMPLETE = "STATUS:  COMMAND EXECUTION COMPLETE";
    private static final String MESSAGE_DFHAC2206 = "DFHAC2206";
    private static final String NO_SYNTAX_MESSAGES = "THERE ARE NO MESSAGES";
    
    private static final String COMMAND_VALUE = "COMMAND";
    private static final String TEXT_VARIABLE_NAME = "&VARIABLE";
    private static final String TEXT_VARIABLE_VALUE = "VARIABLE";
    private static final String PROGRAM_NAME = "MYPROG";
    private static final String CHANNEL_NAME = "CHANNEL";
    private static final String CONTAINER_NAME = "CONTAINER";
    private static final String SPACES = "        ";
    
    @Mock
    private ITerminal ceciTerminalMock;
    
    @Mock
    private ICECIResponse ceciResponseMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setup() throws FieldNotFoundException, KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException {
        ceci = new CECIImpl();
        ceciSpy = Mockito.spy(ceci);

        // Mock all terminal function
        Mockito.when(ceciTerminalMock.type(Mockito.any())).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.enter()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.pf2()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.pf4()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.pf5()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.pf9()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.pf10()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.pf11()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.tab()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.home()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.newLine()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.cursorLeft()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.eraseEof()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.reportScreenWithCursor()).thenReturn(ceciTerminalMock);
    }
    
    @Test
    public void testIssueCommand() throws Exception {
        setupTestIssueCommand();
        
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(COMMAND_EXECUTION_COMPLETE);
        Assert.assertEquals("Error in issueCommand() method", ceciResponseMock, ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE));
        
        PowerMockito.doReturn(COMMAND_VALUE).when(ceciSpy, "retrieveVariableText", Mockito.any(), Mockito.any());
        Assert.assertEquals("Error in issueCommand() method", ceciResponseMock, ceciSpy.issueCommand(ceciTerminalMock, TEXT_VARIABLE_NAME));
        
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn("USER SCREEN").thenReturn("USER SCREEN").thenReturn(COMMAND_EXECUTION_COMPLETE);
        Assert.assertEquals("Error in issueCommand() method", ceciResponseMock, ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE));
    }
    
    @Test
    public void testIssueCommandException1() throws Exception {
        setupTestIssueCommand();
        
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(MESSAGE_DFHAC2206);        
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Command abended - see previous screen");
        ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE);        
    }

    @Test
    public void testIssueCommandException2() throws Exception {
        setupTestIssueCommand();
        
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn("USER SCREEN").thenReturn("USER SCREEN").thenReturn("USER SCREEN");        
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Command failed - see previous screen");
        ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE);
        
    }

    @Test
    public void testIssueCommandException3() throws Exception {
        setupTestIssueCommand();
        
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());        
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Error issuing CECI command");
        ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE);
        
    }

    @Test
    public void testIssueCommandException4() throws Exception {
        setupTestIssueCommand();
        
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());        
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Error issuing CECI command");
        ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE);
        
    }
    
    private void setupTestIssueCommand() throws Exception {        
        PowerMockito.doReturn(0).when(ceciSpy, "defineVariableText", Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn(true).when(ceciSpy, "isCECIScreen");
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "initialScreen");
        PowerMockito.doNothing().when(ceciSpy, "checkForSyntaxMessages");
        PowerMockito.doReturn(ceciResponseMock).when(ceciSpy, "newCECIResponse", Mockito.anyBoolean());        
    }

    @Test
    public void testDefineVariableText() throws Exception {
        PowerMockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy, "validateVariable", Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn(TEXT_VARIABLE_VALUE.length()).when(ceciSpy, "setVariable", Mockito.any(), Mockito.any(), Mockito.any());
        Assert.assertEquals("Error in defineVariableText() method", TEXT_VARIABLE_VALUE.length(), ceciSpy.defineVariableText(ceciTerminalMock, TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE));
    }

    @Test
    public void testDefineVariableBinary() throws Exception {
        PowerMockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy, "validateVariable", Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn(TEXT_VARIABLE_VALUE.length()).when(ceciSpy, "setVariableHex", Mockito.any(), Mockito.any());
        Assert.assertEquals("Error in defineVariableBinary() method", TEXT_VARIABLE_VALUE.length(), ceciSpy.defineVariableBinary(ceciTerminalMock, TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray()));
    }

    @Test
    public void testDefineVariableDoubleWord() throws Exception {
        PowerMockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy, "validateVariable", Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn(99).when(ceciSpy, "setVariable", Mockito.any(), Mockito.any(), Mockito.any());
        Assert.assertEquals("Error in defineVariableDoubleWord() method", 99, ceciSpy.defineVariableDoubleWord(ceciTerminalMock, TEXT_VARIABLE_NAME, 0));
    }

    @Test
    public void testDefineVariableFullWord() throws Exception {
        PowerMockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy, "validateVariable", Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn(99).when(ceciSpy, "setVariable", Mockito.any(), Mockito.any(), Mockito.any());
        Assert.assertEquals("Error in defineVariableFullWord() method", 99, ceciSpy.defineVariableFullWord(ceciTerminalMock, TEXT_VARIABLE_NAME, 0));
    }

    @Test
    public void testDefineVariableHalfWord() throws Exception {
        PowerMockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy, "validateVariable", Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn(99).when(ceciSpy, "setVariable", Mockito.any(), Mockito.any(), Mockito.any());
        Assert.assertEquals("Error in defineVariableHalfWord() method", 99, ceciSpy.defineVariableHalfWord(ceciTerminalMock, TEXT_VARIABLE_NAME, 0));
    }

    @Test
    public void testDefineVariablePacked() throws Exception {
        PowerMockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy, "validateVariable", Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn(99).when(ceciSpy, "setVariable", Mockito.any(), Mockito.any(), Mockito.any());
        Assert.assertEquals("Error in defineVariablePacked() method", 99, ceciSpy.defineVariablePacked(ceciTerminalMock, TEXT_VARIABLE_NAME, 0));
    }

    @Test
    public void testRetrieveVariableText() throws Exception {
        PowerMockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy, "validateVariable", Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn(TEXT_VARIABLE_VALUE).when(ceciSpy, "getVariable", Mockito.any(), Mockito.any());
        Assert.assertEquals("Error in retrieveVariableText() method", TEXT_VARIABLE_VALUE, ceciSpy.retrieveVariableText(ceciTerminalMock, TEXT_VARIABLE_NAME));
    }

    @Test
    public void testRetrieveVariableBinary() throws Exception {
        PowerMockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy, "validateVariable", Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn(TEXT_VARIABLE_VALUE.toCharArray()).when(ceciSpy, "getVariableHex", Mockito.any());
        Assert.assertTrue("Error in retrieveVariableBinary() method", Arrays.equals(TEXT_VARIABLE_VALUE.toCharArray(), ceciSpy.retrieveVariableBinary(ceciTerminalMock, TEXT_VARIABLE_NAME)));
    }

    @Test
    public void testRetrieveVariableDoubleWord() throws Exception {
        PowerMockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy, "validateVariable", Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn("99").when(ceciSpy, "getVariable", Mockito.any(), Mockito.any());
        Assert.assertEquals("Error in retrieveVariableDoubleWord() method", 99L, ceciSpy.retrieveVariableDoubleWord(ceciTerminalMock, TEXT_VARIABLE_NAME));
    }

    @Test
    public void testRetrieveVariableFullWord() throws Exception {
        PowerMockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy, "validateVariable", Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn("99").when(ceciSpy, "getVariable", Mockito.any(), Mockito.any());
        Assert.assertEquals("Error in retrieveVariableFullWord() method", 99, ceciSpy.retrieveVariableFullWord(ceciTerminalMock, TEXT_VARIABLE_NAME));
    }

    @Test
    public void testRetrieveVariableHalfWord()  throws Exception {
        PowerMockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy, "validateVariable", Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn("99").when(ceciSpy, "getVariable", Mockito.any(), Mockito.any());
        Assert.assertEquals("Error in retrieveVariableHalfWord() method", 99, ceciSpy.retrieveVariableHalfWord(ceciTerminalMock, TEXT_VARIABLE_NAME));
    }

    @Test
    public void testRetrieveVariablePacked() throws Exception {
        PowerMockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy, "validateVariable", Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn("99").when(ceciSpy, "getVariable", Mockito.any(), Mockito.any());
        Assert.assertEquals("Error in retrieveVariablePacked() method", 99, ceciSpy.retrieveVariablePacked(ceciTerminalMock, TEXT_VARIABLE_NAME));
    }

    @Test
    public void testDeleteVariable() throws Exception {
        setupTestDeleteVariable();
        
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(SPACES);        
        ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME);
        PowerMockito.verifyPrivate(ceciTerminalMock, Mockito.times(1)).invoke("retrieveScreen");
        
        Mockito.clearInvocations(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(String.format(" %-10s   %+06d   %s", TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.length(), TEXT_VARIABLE_VALUE))
                                                        .thenReturn(SPACES);
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", TEXT_VARIABLE_NAME));
        ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME);
        PowerMockito.verifyPrivate(ceciTerminalMock, Mockito.times(2)).invoke("retrieveScreen");
        
        Mockito.clearInvocations(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(String.format(" %-10s   %+06d   %s", TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.length(), TEXT_VARIABLE_VALUE))
                                                       .thenReturn(SPACES);
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", "XXXX"))
                                                              .thenReturn(String.format("%-10s", TEXT_VARIABLE_NAME));
        ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME);
        PowerMockito.verifyPrivate(ceciTerminalMock, Mockito.times(2)).invoke("retrieveScreen");
        ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME.substring(1));
        PowerMockito.verifyPrivate(ceciTerminalMock, Mockito.times(3)).invoke("retrieveScreen");
    }

    @Test
    public void testDeleteVariableException1() throws Exception {
        setupTestDeleteVariable();
        
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(String.format(" %-10s   %+06d   %s", TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.length(), TEXT_VARIABLE_VALUE));
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", "XXXX"))
                                                              .thenReturn("PF");
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to find variable to delete");
        ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME);
    }

    @Test
    public void testDeleteVariableException2() throws Exception {
        setupTestDeleteVariable();
        
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(String.format(" %-10s   %+06d   %s", TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.length(), TEXT_VARIABLE_VALUE));
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", "XXXX"))
                                                              .thenReturn(String.format("%-10s", TEXT_VARIABLE_NAME));
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Delete variable failed");
        ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME);
    }

    @Test
    public void testDeleteVariableException3() throws Exception {
        setupTestDeleteVariable();
        
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(String.format(" %-10s   %+06d   %s", TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.length(), TEXT_VARIABLE_VALUE));
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", TEXT_VARIABLE_NAME));
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to delete variable");
        ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME);
    }

    @Test
    public void testDeleteVariableException4() throws Exception {
        setupTestDeleteVariable();
        
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(String.format(" %-10s   %+06d   %s", TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.length(), TEXT_VARIABLE_VALUE));
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", TEXT_VARIABLE_NAME));
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to delete variable");
        ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME);
    }

    @Test
    public void testDeleteAllVariables() throws Exception {
        setupTestDeleteVariable();
        
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("");
        ceciSpy.deleteAllVariables(ceciTerminalMock);
        PowerMockito.verifyPrivate(ceciTerminalMock, Mockito.times(55)).invoke("tab");
        
        Mockito.clearInvocations(ceciTerminalMock);        
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("PF");
        ceciSpy.deleteAllVariables(ceciTerminalMock);
        PowerMockito.verifyPrivate(ceciTerminalMock, Mockito.times(1)).invoke("tab");
    }

    @Test
    public void testDeleteAllVariablesException1() throws Exception {
        setupTestDeleteVariable();
        
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to delete all variables");
        ceciSpy.deleteAllVariables(ceciTerminalMock);
    }

    @Test
    public void testDeleteAllVariablesException2() throws Exception {
        setupTestDeleteVariable();
        
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to delete all variables");
        ceciSpy.deleteAllVariables(ceciTerminalMock);
    }

    private void setupTestDeleteVariable() throws Exception {
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "hexOff");
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "variableScreen");
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "multipleTab", Mockito.anyInt());
    }

    @Test
    public void testGetEIB() throws Exception {
        setupTestGetEIB();
        
        Assert.assertTrue("Error in getEIB() method",  ceciSpy.getEIB(ceciTerminalMock) instanceof CECIExecInterfaceBlockImpl);
    }

    @Test
    public void testGetEIBException1() throws Exception {
        setupTestGetEIB();
        
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to navigate to EIB screen");        
        ceciSpy.getEIB(ceciTerminalMock);
    }

    @Test
    public void testGetEIBException2() throws Exception {
        setupTestGetEIB();
        
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to navigate to EIB screen");        
        ceciSpy.getEIB(ceciTerminalMock);
    }

    private void setupTestGetEIB() throws Exception {
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "hexOff");
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "hexOn");
    }

    @Test
    public void testLinkProgram() throws Exception {
        PowerMockito.doReturn(ceciResponseMock).when(ceciSpy, "issueCommand", Mockito.any(), Mockito.any());
        
        Assert.assertEquals("Error in linkProgram() method", ceciResponseMock, ceciSpy.linkProgram(ceciTerminalMock, PROGRAM_NAME, null, null, null, false));
        
        Assert.assertEquals("Error in linkProgram() method", ceciResponseMock, ceciSpy.linkProgram(ceciTerminalMock, PROGRAM_NAME, TEXT_VARIABLE_NAME, "SYSID", null, false));
        
        Assert.assertEquals("Error in linkProgram() method", ceciResponseMock, ceciSpy.linkProgram(ceciTerminalMock, PROGRAM_NAME, TEXT_VARIABLE_NAME, null, "TRAN", false));
        
        Assert.assertEquals("Error in linkProgram() method", ceciResponseMock, ceciSpy.linkProgram(ceciTerminalMock, PROGRAM_NAME, TEXT_VARIABLE_NAME, null, null, true));

        PowerMockito.doReturn(0).when(ceciSpy, "defineVariableText", Mockito.any(), Mockito.any(), Mockito.any());
        Assert.assertEquals("Error in linkProgram() method", ceciResponseMock, ceciSpy.linkProgram(ceciTerminalMock, PROGRAM_NAME, "COMMAREA", null, null, false));
    }

    @Test
    public void testLinkProgramWithChannel() throws Exception {
        PowerMockito.doReturn(ceciResponseMock).when(ceciSpy, "issueCommand", Mockito.any(), Mockito.any());
        
        Assert.assertEquals("Error in linkProgramWithChannel() method", ceciResponseMock, ceciSpy.linkProgramWithChannel(ceciTerminalMock, PROGRAM_NAME, null, null, null, false));
        
        Assert.assertEquals("Error in linkProgramWithChannel() method", ceciResponseMock, ceciSpy.linkProgramWithChannel(ceciTerminalMock, PROGRAM_NAME, CHANNEL_NAME, "SYSID", null, false));
        
        Assert.assertEquals("Error in linkProgramWithChannel() method", ceciResponseMock, ceciSpy.linkProgramWithChannel(ceciTerminalMock, PROGRAM_NAME, CHANNEL_NAME, null, "TRAN", false));
        
        Assert.assertEquals("Error in linkProgramWithChannel() method", ceciResponseMock, ceciSpy.linkProgramWithChannel(ceciTerminalMock, PROGRAM_NAME, CHANNEL_NAME, null, null, true));

        PowerMockito.doReturn(0).when(ceciSpy, "defineVariableText", Mockito.any(), Mockito.any(), Mockito.any());
        Assert.assertEquals("Error in linkProgramWithChannel() method", ceciResponseMock, ceciSpy.linkProgramWithChannel(ceciTerminalMock, PROGRAM_NAME, "CHANNEL_NAME", null, null, false));
    }

    @Test
    public void testPutContainer() throws Exception {
        PowerMockito.doReturn(ceciResponseMock).when(ceciSpy, "issueCommand", Mockito.any(), Mockito.any());
        PowerMockito.doReturn(TEXT_VARIABLE_VALUE.length()).when(ceciSpy, "setVariable", Mockito.any(), Mockito.any(), Mockito.any());
        
        Assert.assertEquals("Error in putContainer() method", ceciResponseMock, ceciSpy.putContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_VALUE, null, null, null));
        
        Assert.assertEquals("Error in putContainer() method", ceciResponseMock, ceciSpy.putContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, null, null, null));
        
        Assert.assertEquals("Error in putContainer() method", ceciResponseMock, ceciSpy.putContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, "CHAR", null, null));
        
        Assert.assertEquals("Error in putContainer() method", ceciResponseMock, ceciSpy.putContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, null, "CCID", null));
        
        Assert.assertEquals("Error in putContainer() method", ceciResponseMock, ceciSpy.putContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, null, null, "CODEPAGE"));
    }

    @Test
    public void testGetContainer() throws Exception {
        PowerMockito.doReturn(ceciResponseMock).when(ceciSpy, "issueCommand", Mockito.any(), Mockito.any());
        PowerMockito.doReturn(TEXT_VARIABLE_VALUE.length()).when(ceciSpy, "setVariable", Mockito.any(), Mockito.any(), Mockito.any());
        
        Assert.assertEquals("Error in getContainer() method", ceciResponseMock, ceciSpy.getContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, null, null));
        
        Assert.assertEquals("Error in getContainer() method", ceciResponseMock, ceciSpy.getContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, "CCID", null));
        
        Assert.assertEquals("Error in getContainer() method", ceciResponseMock, ceciSpy.getContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, null, "CODEPAGE"));
        
        Assert.assertEquals("Error in getContainer() method", ceciResponseMock, ceciSpy.getContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME.substring(1), null, null));
    }
    
    @Test
    public void testInitialScreen() throws Exception {
        setupTestInitialScreen();
        
        PowerMockito.doReturn(true).when(ceciSpy, "isCECIScreen");
        PowerMockito.doReturn(false).when(ceciSpy, "isHelpScreen", Mockito.any());
        Assert.assertEquals("Error in initialScreen() method", ceciTerminalMock, ceciSpy.initialScreen());

        PowerMockito.doReturn(true).when(ceciSpy, "isHelpScreen", Mockito.any());
        Assert.assertEquals("Error in initialScreen() method", ceciTerminalMock, ceciSpy.initialScreen());

        PowerMockito.when(ceciSpy, "isCECIScreen").thenReturn(false).thenReturn(true);
        Assert.assertEquals("Error in initialScreen() method", ceciTerminalMock, ceciSpy.initialScreen());
    }
    
    @Test
    public void testInitialScreenException1() throws Exception {
        setupTestInitialScreen();

        PowerMockito.doReturn(false).when(ceciSpy, "isCECIScreen");       
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Cannot identify terminal as CECI session");
        ceciSpy.initialScreen();
    }
    
    @Test
    public void testInitialScreenException2() throws Exception {
        setupTestInitialScreen();

        PowerMockito.doReturn(false).when(ceciSpy, "isCECIScreen");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to navigate to CECI initial screen");
        ceciSpy.initialScreen();
    }
    
    @Test
    public void testInitialScreenException3() throws Exception {
        setupTestInitialScreen();

        PowerMockito.doReturn(false).when(ceciSpy, "isCECIScreen");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to navigate to CECI initial screen");
        ceciSpy.initialScreen();
    }
    
    private void setupTestInitialScreen() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
    }

    @Test
    public void testVariableScreen() throws Exception {
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "initialScreen");
        Assert.assertEquals("Error in variableScreen() method", ceciTerminalMock, ceciSpy.variableScreen());
    }
    
    @Test
    public void testVariableScreenException1() throws Exception {
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "initialScreen");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to navigate to CECI variables screen");
        ceciSpy.variableScreen();
    }
    
    @Test
    public void testVariableScreenException2() throws Exception {
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "initialScreen");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to navigate to CECI variables screen");
        ceciSpy.variableScreen();
    }
    
    @Test
    public void testIsCECIScreen() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        PowerMockito.doReturn(true).when(ceciSpy, "isInitialScreen", Mockito.any());
        PowerMockito.doReturn(true).when(ceciSpy, "isHelpScreen", Mockito.any());
        PowerMockito.doReturn(true).when(ceciSpy, "isCommandBeforeScreen", Mockito.any());
        PowerMockito.doReturn(true).when(ceciSpy, "isCommandAfterScreen", Mockito.any());
        PowerMockito.doReturn(true).when(ceciSpy, "isEibScreen", Mockito.any());
        PowerMockito.doReturn(true).when(ceciSpy, "isVariablesScreen", Mockito.any());
        PowerMockito.doReturn(true).when(ceciSpy, "isVariablesExpansionScreen", Mockito.any());
        PowerMockito.doReturn(true).when(ceciSpy, "isMsgScreen", Mockito.any());
        Assert.assertTrue("Error in isCECIScreen() method", ceciSpy.isCECIScreen());
        
        PowerMockito.doReturn(false).when(ceciSpy, "isInitialScreen", Mockito.any());
        Assert.assertTrue("Error in isCECIScreen() method", ceciSpy.isCECIScreen());

        PowerMockito.doReturn(false).when(ceciSpy, "isHelpScreen", Mockito.any());
        Assert.assertTrue("Error in isCECIScreen() method", ceciSpy.isCECIScreen());

        PowerMockito.doReturn(false).when(ceciSpy, "isCommandBeforeScreen", Mockito.any());
        Assert.assertTrue("Error in isCECIScreen() method", ceciSpy.isCECIScreen());

        PowerMockito.doReturn(false).when(ceciSpy, "isCommandAfterScreen", Mockito.any());
        Assert.assertTrue("Error in isCECIScreen() method", ceciSpy.isCECIScreen());

        PowerMockito.doReturn(false).when(ceciSpy, "isEibScreen", Mockito.any());
        Assert.assertTrue("Error in isCECIScreen() method", ceciSpy.isCECIScreen());

        PowerMockito.doReturn(false).when(ceciSpy, "isVariablesScreen", Mockito.any());
        Assert.assertTrue("Error in isCECIScreen() method", ceciSpy.isCECIScreen());

        PowerMockito.doReturn(false).when(ceciSpy, "isVariablesExpansionScreen", Mockito.any());
        Assert.assertTrue("Error in isCECIScreen() method", ceciSpy.isCECIScreen());

        PowerMockito.doReturn(false).when(ceciSpy, "isMsgScreen", Mockito.any());
        Assert.assertFalse("Error in isCECIScreen() method", ceciSpy.isCECIScreen());

    }
    
    @Test
    public void testIsInitialScreen() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Assert.assertFalse("Error in isInitialScreen() method", ceciSpy.isInitialScreen(SPACES));
    }
    
    @Test
    public void testIsCommandBeforeScreen() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Assert.assertFalse("Error in isCommandBeforeScreen() method", ceciSpy.isCommandBeforeScreen(SPACES));
    }
    
    @Test
    public void testIsCommandAfterScreen() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Assert.assertFalse("Error in isCommandAfterScreen() method", ceciSpy.isCommandAfterScreen(SPACES));
    }
    
    @Test
    public void testIsHelpScreen() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Assert.assertFalse("Error in isHelpScreen() method", ceciSpy.isHelpScreen(SPACES));
    }
    
    @Test
    public void testIsEibScreen() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Assert.assertFalse("Error in isEibScreen() method", ceciSpy.isEibScreen(SPACES));
    }
    
    @Test
    public void testIsVariablesScreen() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Assert.assertFalse("Error in isVariablesScreen() method", ceciSpy.isVariablesScreen(SPACES));
    }
    
    @Test
    public void testIsMsgScreen() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Assert.assertFalse("Error in isMsgScreen() method", ceciSpy.isMsgScreen(SPACES));
    }
    
    @Test
    public void testIsVariablesExpansionScreen() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Assert.assertFalse("Error in isVariablesExpansionScreen() method", ceciSpy.isVariablesExpansionScreen(SPACES));
    }

    @Test
    public void testMultipleTab() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        
        Assert.assertEquals("Error in multipleTab() method", ceciTerminalMock, ceciSpy.multipleTab(1));
    }

    @Test
    public void testCheckForSyntaxMessages() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(NO_SYNTAX_MESSAGES);
        
        ceciSpy.checkForSyntaxMessages();
        PowerMockito.verifyPrivate(ceciTerminalMock, Mockito.times(1)).invoke("enter");
    }

    @Test
    public void testCheckForSyntaxMessagesException1() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Whitebox.setInternalState(ceciSpy, "command", COMMAND_VALUE);
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(SPACES);       
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Command failed syntax check. \nCommand:\n  " + COMMAND_VALUE + "\nSyntax Error Screen:\n" + SPACES);
        
        ceciSpy.checkForSyntaxMessages();
    }

    @Test
    public void testCheckForSyntaxMessagesException2() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(NO_SYNTAX_MESSAGES);        
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());        
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to check for syntax messages");
        
        ceciSpy.checkForSyntaxMessages();
    }

    @Test
    public void testCheckForSyntaxMessagesException3() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(NO_SYNTAX_MESSAGES);        
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());        
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to check for syntax messages");
        
        ceciSpy.checkForSyntaxMessages();
    }

    @Test
    public void testValidateVariable() throws Exception {
        Assert.assertEquals("Error in validateVariable() method", TEXT_VARIABLE_NAME, ceciSpy.validateVariable(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray(), null));

        Assert.assertEquals("Error in validateVariable() method", TEXT_VARIABLE_NAME, ceciSpy.validateVariable(TEXT_VARIABLE_NAME.substring(1), TEXT_VARIABLE_VALUE.toCharArray(), Integer.toString(TEXT_VARIABLE_VALUE.length())));
        
        Assert.assertEquals("Error in validateVariable() method", TEXT_VARIABLE_NAME, ceciSpy.validateVariable(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray(), null));
        
        Assert.assertEquals("Error in validateVariable() method", TEXT_VARIABLE_NAME, ceciSpy.validateVariable(TEXT_VARIABLE_NAME, null, null));
        
        String name = "&234567890";
        Assert.assertEquals("Error in validateVariable() method", name, ceciSpy.validateVariable(name, TEXT_VARIABLE_VALUE.toCharArray(), null));
        
        name = "&azAZ09@#";
        Assert.assertEquals("Error in validateVariable() method", name, ceciSpy.validateVariable(name, TEXT_VARIABLE_VALUE.toCharArray(), null));
    }

    @Test
    public void testValidateVariableException1() throws Exception {
        String name = "&2345678901";
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("CECI variable name \"" + name + "\" greater than maximum length of 10 characters including the leading \"&\"");
        
        ceciSpy.validateVariable(name, TEXT_VARIABLE_VALUE.toCharArray(), null);
    }

    @Test
    public void testValidateVariableException2() throws Exception {
        String name = "&?";
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("CECI variable name \"" + name + "\" invalid must. Must start with \"&\" and can contain one of more [a-zA-Z0-9@#]");
        
        ceciSpy.validateVariable(name, TEXT_VARIABLE_VALUE.toCharArray(), null);
    }

    @Test
    public void testValidateVariableException3() throws Exception {
        String value = "123456789";
        String type = "H";
        int maxLength = 6;
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("CECI variable value length " + value.length() + " greater than maximum of " + maxLength +  " for type \"" + type + "\"" );
        
        ceciSpy.validateVariable(TEXT_VARIABLE_NAME, value.toCharArray(), type);
    }

    @Test
    public void testValidateVariableException4() throws Exception {        
        String value = new String(new char[32768]).replace("\0", "X");
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("CECI variable value length " + value.length() + " greater than maximum 32767");
        ceciSpy.validateVariable(TEXT_VARIABLE_NAME, value.toCharArray(), null);
    }
    
    @Test
    public void testSetVariable() throws Exception {
        setupTestVariable();
        PowerMockito.doReturn(TEXT_VARIABLE_VALUE.length()).when(ceciSpy, "setVariableOnPage", Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", "X")).thenReturn(String.format("%-10s", " "));
        Assert.assertEquals("Error in setVariable() method", TEXT_VARIABLE_VALUE.length(), ceciSpy.setVariable(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE, null));
        
        Assert.assertEquals("Error in setVariable() method", TEXT_VARIABLE_VALUE.length(), ceciSpy.setVariable(TEXT_VARIABLE_NAME + "X", TEXT_VARIABLE_VALUE, null));
        
        Assert.assertEquals("Error in setVariable() method", TEXT_VARIABLE_VALUE.length(), ceciSpy.setVariable(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE, "H"));
        
        int length = 1281;
        Mockito.when(ceciSpy.setVariableOnPage(Mockito.any(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(20).thenReturn(21);
        Assert.assertEquals("Error in setVariable() method", length, ceciSpy.setVariable(TEXT_VARIABLE_NAME, new String(new char[length]).replace("\0", "X"), null));
    }
    
    @Test
    public void testSetVariableException1() throws Exception {
        setupTestVariable();
        PowerMockito.doReturn("PF").when(ceciTerminalMock, "retrieveFieldAtCursor");
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("No space on CECI variable screen for new variables");
        
        ceciSpy.setVariable(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE, null);
    }
    
    @Test
    public void testSetVariableException2() throws Exception {
        setupTestVariable();
        PowerMockito.doReturn(String.format("%-10s", " ")).when(ceciTerminalMock, "retrieveFieldAtCursor");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());    
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to set CECI variable");
        
        ceciSpy.setVariable(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE, null);
    }
    
    @Test
    public void testSetVariableException3() throws Exception {
        setupTestVariable();
        PowerMockito.doReturn(String.format("%-10s", " ")).when(ceciTerminalMock, "retrieveFieldAtCursor");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());    
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to set CECI variable");
        
        ceciSpy.setVariable(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE, null);
    }

    private void setupTestVariable() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        PowerMockito.doNothing().when(ceciSpy, "deleteVariable", Mockito.any(), Mockito.any());
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "hexOff");
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "variableScreen");
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "moveToVariable", Mockito.any());
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "multipleTab", Mockito.anyInt());
    }

    @Test
    public void testSetVariableOnPage() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        
        String[] chunks = new String[] {TEXT_VARIABLE_VALUE, TEXT_VARIABLE_VALUE};
        int start = 0;
        int numberOfLines = 1;
        Assert.assertEquals("Error in setVariableOnPage() method", numberOfLines, ceciSpy.setVariableOnPage(chunks, start, numberOfLines));
        
        numberOfLines = 2;
        Assert.assertEquals("Error in setVariableOnPage() method", numberOfLines, ceciSpy.setVariableOnPage(chunks, start, numberOfLines));
        
        chunks = new String[] {String.format("%-65s", "X"), TEXT_VARIABLE_VALUE};
        Assert.assertEquals("Error in setVariableOnPage() method", numberOfLines, ceciSpy.setVariableOnPage(chunks, start, numberOfLines));
    }

    @Test
    public void testSetVariableOnPageException() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Mockito.when(ceciTerminalMock.type(Mockito.any())).thenThrow(new FieldNotFoundException());    
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable enter variable data");
        
        String[] chunks = new String[] {TEXT_VARIABLE_VALUE, TEXT_VARIABLE_VALUE};
        int start = 0;
        int numberOfLines = 1;
        ceciSpy.setVariableOnPage(chunks, start, numberOfLines);
    }
    
    @Test
    public void testSetVariableHex() throws Exception {
        setupTestVariable();
        PowerMockito.doReturn(TEXT_VARIABLE_VALUE.length()).when(ceciSpy, "setVariableHexOnPage", Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", "X")).thenReturn(String.format("%-10s", " "));        
        Assert.assertEquals("Error in setVariableHex() method", TEXT_VARIABLE_VALUE.length(), ceciSpy.setVariableHex(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray()));
        
        Assert.assertEquals("Error in setVariableHex() method", TEXT_VARIABLE_VALUE.length(), ceciSpy.setVariableHex(TEXT_VARIABLE_NAME + "X", TEXT_VARIABLE_VALUE.toCharArray()));
        
        int length = 21;
        char[] value = new String(new char[length]).replace("\0", "X").toCharArray();
        Mockito.when(ceciSpy.setVariableHexOnPage(Mockito.any(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(20).thenReturn(21);
        Assert.assertEquals("Error in setVariableHex() method", length, ceciSpy.setVariableHex(TEXT_VARIABLE_NAME, value));
    }
    
    @Test
    public void testSetVariableHexException1() throws Exception {
        setupTestVariable();
        PowerMockito.doReturn("PF").when(ceciTerminalMock, "retrieveFieldAtCursor");
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("No space on CECI variable screen for new variables");
        
        ceciSpy.setVariableHex(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray());
    }
    
    @Test
    public void testSetVariableHexException2() throws Exception {
        setupTestVariable();
        PowerMockito.doReturn(String.format("%-10s", " ")).when(ceciTerminalMock, "retrieveFieldAtCursor");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());    
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to set CECI binary variable");
        
        ceciSpy.setVariableHex(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray());
    }
    
    @Test
    public void testSetVariableHexException3() throws Exception {
        setupTestVariable();
        PowerMockito.doReturn(String.format("%-10s", " ")).when(ceciTerminalMock, "retrieveFieldAtCursor");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());    
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to set CECI binary variable");
        
        ceciSpy.setVariableHex(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray());
    }

    @Test
    public void testSetVariableHexOnPage() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        
        char[] value = TEXT_VARIABLE_VALUE.toCharArray();
        int start = 0;
        int numberOfLines = 1;
        Assert.assertEquals("Error in setVariableHexOnPage() method", value.length, ceciSpy.setVariableHexOnPage(value, start, numberOfLines));
        
        value = new String(new char[32]).replace("\0", "X").toCharArray();
        numberOfLines = 2;
        Assert.assertEquals("Error in setVariableHexOnPage() method", value.length, ceciSpy.setVariableHexOnPage(value, start, numberOfLines));
        
        numberOfLines = -1;
        Assert.assertEquals("Error in setVariableHexOnPage() method", 0, ceciSpy.setVariableHexOnPage(value, start, numberOfLines));
        
        value = new char[0];
        numberOfLines = -1;
        Assert.assertEquals("Error in setVariableHexOnPage() method", value.length, ceciSpy.setVariableHexOnPage(value, start, numberOfLines));
    }

    @Test
    public void testSetVariableHexOnPageException() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Mockito.when(ceciTerminalMock.type(Mockito.any())).thenThrow(new FieldNotFoundException());    
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable enter variable data");
        
        char[] value = TEXT_VARIABLE_VALUE.toCharArray();
        int start = 0;
        int numberOfLines = 1;
        ceciSpy.setVariableHexOnPage(value, start, numberOfLines);
    }
    
    @Test
    public void testGetVariable() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%+06d", TEXT_VARIABLE_VALUE.length()));
        PowerMockito.doReturn(TEXT_VARIABLE_VALUE).when(ceciSpy, "getVariableFromPage", Mockito.anyInt(), Mockito.anyInt());
        
        Assert.assertEquals("Error in getVariable() method", TEXT_VARIABLE_VALUE, ceciSpy.getVariable(TEXT_VARIABLE_NAME, null));

        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%+06d", TEXT_VARIABLE_VALUE.length()*2));
        Assert.assertEquals("Error in getVariable() method", TEXT_VARIABLE_VALUE + TEXT_VARIABLE_VALUE, ceciSpy.getVariable(TEXT_VARIABLE_NAME, null));

        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("H");
        String value = String.format("%012d", 0);
        PowerMockito.doReturn(value).when(ceciSpy, "getVariableFromPage", Mockito.anyInt(), Mockito.anyInt());
        Assert.assertEquals("Error in getVariable() method", value, ceciSpy.getVariable(TEXT_VARIABLE_NAME, "H"));
    }
    
    @Test
    public void testGetVariableException1() throws Exception {
        setupTestGetVariable();
        String lengthString = "XXXX";
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(lengthString);    
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unexpected variable type \"" + lengthString  + "\" for \"" + TEXT_VARIABLE_NAME + "\"");
        String type = "H";
        
        ceciSpy.getVariable(TEXT_VARIABLE_NAME, type);
    }
    
    @Test
    public void testGetVariableException2() throws Exception {
        setupTestGetVariable();
        String lengthString = "XXXX";
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(lengthString);   
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to determine variable field length");
        
        ceciSpy.getVariable(TEXT_VARIABLE_NAME, null);
    }
    
    @Test
    public void testGetVariableException3() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());    
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to get CECI variable");
        
        ceciSpy.getVariable(TEXT_VARIABLE_NAME, null);
    }
    
    @Test
    public void testGetVariableException4() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());    
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to get CECI variable");
        
        ceciSpy.getVariable(TEXT_VARIABLE_NAME, null);
    }

    private void setupTestGetVariable() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "moveToVariable", Mockito.any());
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "multipleTab", Mockito.anyInt());
    }
    
    @Test
    public void getVariableFromPage() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(TEXT_VARIABLE_VALUE);

        Assert.assertEquals("Error in getVariableFromPage() method", TEXT_VARIABLE_VALUE, ceciSpy.getVariableFromPage(TEXT_VARIABLE_VALUE.length(), 1));
        
        Assert.assertEquals("Error in getVariableFromPage() method", TEXT_VARIABLE_VALUE, ceciSpy.getVariableFromPage(TEXT_VARIABLE_VALUE.length()*2, 1));
        
    }
    
    @Test
    public void getVariableFromPageException1() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(TEXT_VARIABLE_VALUE);
        Mockito.when(ceciTerminalMock.tab()).thenThrow(new FieldNotFoundException());   
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to get variable from page");
        
        ceciSpy.getVariableFromPage(1, 1);
        
    }
    
    @Test
    public void testGetVariableHex() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%+06d", TEXT_VARIABLE_VALUE.length())).thenReturn(TEXT_VARIABLE_VALUE);
        PowerMockito.doReturn(TEXT_VARIABLE_VALUE).when(ceciSpy, "getVariableHexFromPage", Mockito.anyInt(), Mockito.anyInt());
        
        Assert.assertTrue("Error in getVariableHex() method", Arrays.equals(TEXT_VARIABLE_VALUE.toCharArray(), ceciSpy.getVariableHex(TEXT_VARIABLE_VALUE)));
        
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%+06d", TEXT_VARIABLE_VALUE.length()*2)).thenReturn(TEXT_VARIABLE_VALUE);        
        Assert.assertTrue("Error in getVariableHex() method", Arrays.equals((TEXT_VARIABLE_VALUE + TEXT_VARIABLE_VALUE).toCharArray(), ceciSpy.getVariableHex(TEXT_VARIABLE_VALUE)));
    }
    
    @Test
    public void testGetVariableHexException1() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("XXXX");   
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to determine variable field length");
        
        ceciSpy.getVariableHex(TEXT_VARIABLE_VALUE);
    }
    
    @Test
    public void testGetVariableHexException2() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to get CECI binary variable");
        
        ceciSpy.getVariableHex(TEXT_VARIABLE_VALUE);
    }
    
    @Test
    public void testGetVariableHexException3() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to get CECI binary variable");
        
        ceciSpy.getVariableHex(TEXT_VARIABLE_VALUE);
    }
    
    @Test
    public void testGetVariableHexPage() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("5A5A5A5A");
        
        Assert.assertEquals("Error in getVariableHexFromPage() method", "ZZZZ", ceciSpy.getVariableHexFromPage(4, 1));
        
        Assert.assertEquals("Error in getVariableHexFromPage() method", "ZZZZZZZZZZZZZZZZ", ceciSpy.getVariableHexFromPage(20, 1));
    }
    
    @Test
    public void testGetVariableHexPageException1() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("5A5A5A5A");
        Mockito.when(ceciTerminalMock.tab()).thenThrow(new FieldNotFoundException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to get binary variable from page");
        
        ceciSpy.getVariableHexFromPage(4, 1);
    }
    
    @Test
    public void testMoveToVariable() throws Exception {
        setupMoveToVariable();
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(TEXT_VARIABLE_NAME + " "); 
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", "X")).thenReturn(String.format("%-10s", TEXT_VARIABLE_NAME));

        Assert.assertEquals("Error in moveToVariable() method", ceciTerminalMock, ceciSpy.moveToVariable(TEXT_VARIABLE_NAME));
    }
    
    @Test
    public void testMoveToVariableException1() throws Exception {
        setupMoveToVariable();
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(" "); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to find variable " + TEXT_VARIABLE_NAME);
    
        ceciSpy.moveToVariable(TEXT_VARIABLE_NAME);
    }
    
    @Test
    public void testMoveToVariableException2() throws Exception {
        setupMoveToVariable();
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(TEXT_VARIABLE_NAME + " "); 
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("PF");
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to find variable " + TEXT_VARIABLE_NAME);
    
        ceciSpy.moveToVariable(TEXT_VARIABLE_NAME);
    }
    
    @Test
    public void testMoveToVariableException3() throws Exception {
        setupMoveToVariable();
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(TEXT_VARIABLE_NAME + " ");
        Mockito.when(ceciTerminalMock.newLine()).thenThrow(new FieldNotFoundException());  
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Problem serching for variable " + TEXT_VARIABLE_NAME);
    
        ceciSpy.moveToVariable(TEXT_VARIABLE_NAME);
    }

    private void setupMoveToVariable() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "hexOff");
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "multipleTab", Mockito.anyInt());
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "variableScreen");        
    }
    
    @Test
    public void testIsHexOn() throws CECIException {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(" EIBTIME      = X'00");
        
        Assert.assertTrue("Error in isHexOn() method", ceciSpy.isHexOn());        
    }
    
    @Test
    public void testIsHexOnException1() throws CECIException, TimeoutException, KeyboardLockedException, TerminalInterruptedException {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(" EIBTIME      = X'00");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to determine if CECI is in HEX mode");
        
        ceciSpy.isHexOn();        
    }
    
    @Test
    public void testIsHexOnException2() throws CECIException, TimeoutException, KeyboardLockedException, TerminalInterruptedException {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(" EIBTIME      = X'00");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to determine if CECI is in HEX mode");
        
        ceciSpy.isHexOn();          
    }
    
    @Test
    public void testHexOn() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        PowerMockito.doReturn(true).when(ceciSpy, "isHexOn");
        
        Assert.assertEquals("Error in hexOn() method", ceciTerminalMock, ceciSpy.hexOn());  
        
        PowerMockito.doReturn(false).when(ceciSpy, "isHexOn");
        Assert.assertEquals("Error in hexOn() method", ceciTerminalMock, ceciSpy.hexOn());        
    }
    
    @Test
    public void testHexOnException1() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        PowerMockito.doReturn(false).when(ceciSpy, "isHexOn");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to set CECI HEX ON");
        
        ceciSpy.hexOn();        
    }
    
    @Test
    public void testHexOnException2() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        PowerMockito.doReturn(false).when(ceciSpy, "isHexOn");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to set CECI HEX ON");
        
        ceciSpy.hexOn();          
    }
    
    @Test
    public void testHexOff() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        PowerMockito.doReturn(true).when(ceciSpy, "isHexOn");
        
        Assert.assertEquals("Error in hexOff() method", ceciTerminalMock, ceciSpy.hexOff());  
        
        PowerMockito.doReturn(false).when(ceciSpy, "isHexOn");
        Assert.assertEquals("Error in hexOff() method", ceciTerminalMock, ceciSpy.hexOff());        
    }
    
    @Test
    public void testHexOffException1() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        PowerMockito.doReturn(true).when(ceciSpy, "isHexOn");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to set CECI HEX OFF");
        
        ceciSpy.hexOff();        
    }
    
    @Test
    public void testHexOffException2() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        PowerMockito.doReturn(true).when(ceciSpy, "isHexOn");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to set CECI HEX OFF");
        
        ceciSpy.hexOff();          
    }
    
    @Test
    public void testNewCECIResponse() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn("   RESPONSE: FILENOTFOUND          EIBRESP=+0000000012 EIBRESP2=+0000000001     ");
        PowerMockito.doReturn(new LinkedHashMap<>()).when(ceciSpy, "parseResponseOutput");
        
        ICECIResponse ceciResponse = ceciSpy.newCECIResponse(false);
        
        ceciSpy.newCECIResponse(true);
        Assert.assertEquals("Error in newCECIResponse() method", "FILENOTFOUND", ceciResponse.getResponse());
        Assert.assertEquals("Error in newCECIResponse() method", 12, ceciResponse.getEIBRESP()); 
        Assert.assertEquals("Error in newCECIResponse() method", 1, ceciResponse.getEIBRESP2()); 
    }
    
    @Test
    public void testParseResponseOutput() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "multipleTab", Mockito.anyInt());
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("OPTION1").thenReturn("OPTION2");
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn("OPTION= OPTION1      LENGTH= +00008 ").thenReturn("OPTION= OPTION2      LENGTH= +00008 ");
        PowerMockito.doReturn(new ResponseOutputValueImpl("XXXXXXXX")).when(ceciSpy, "getOptionValue", Mockito.any());
        
        Assert.assertTrue("Error in parseResponseOutput() method", ceciSpy.parseResponseOutput().containsKey("OPTION1"));
        
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("OPTION3").thenReturn("OPTION4").thenReturn("PF");
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn("OPTION= OPTION3      LENGTH= +00008 ").thenReturn("OPTION= OPTION4      LENGTH= +00008 ");
        Assert.assertTrue("Error in parseResponseOutput() method", ceciSpy.parseResponseOutput().containsKey("OPTION4"));
    }
    
    @Test
    public void testParseResponseOutputException1() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "multipleTab", Mockito.anyInt());
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("OPTION1");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to parse command output");
        
        ceciSpy.parseResponseOutput();
    }
    
    @Test
    public void testParseResponseOutputException2() throws Exception {
        Whitebox.setInternalState(ceciSpy, "terminal", ceciTerminalMock);
        PowerMockito.doReturn(ceciTerminalMock).when(ceciSpy, "multipleTab", Mockito.anyInt());
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("OPTION1");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to parse command output");
        
        ceciSpy.parseResponseOutput();
    }
    
    @Test
    public void testGetOptionValue() throws Exception {
        setupTestGetVariable();
        String value = "000000";
        PowerMockito.doReturn(value).when(ceciSpy, "getVariableFromPage", Mockito.anyInt(), Mockito.anyInt());        
        String screen = "OPTION= LENGTH       LENGTH= H ";

        Assert.assertEquals("Error in getOptionValue() method", value, ceciSpy.getOptionValue(screen).getTextValue());
        
        PowerMockito.doReturn("000").when(ceciSpy, "getVariableFromPage", Mockito.anyInt(), Mockito.anyInt());
        Assert.assertEquals("Error in getOptionValue() method", value, ceciSpy.getOptionValue(screen).getTextValue());
        
        screen = "OPTION= FROM         LENGTH= +00001 ";
        PowerMockito.doReturn("F1").when(ceciSpy, "getOptionValueInHex", Mockito.anyInt(), Mockito.anyInt());
        Assert.assertTrue("Error in getOptionValue() method", Arrays.equals(new char[] {'F', '1'}, ceciSpy.getOptionValue(screen).getHexValue()));
    }
    
    @Test
    public void testGetOptionValueException1() throws Exception {
        setupTestGetVariable();
        PowerMockito.doReturn("000").when(ceciSpy, "getVariableFromPage", Mockito.anyInt(), Mockito.anyInt());        
        String screen = "OPTION= LENGTH       LENGTH= H ";
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to parse command output option value");

        ceciSpy.getOptionValue(screen);
    }
    
    @Test
    public void testGetOptionValueException2() throws Exception {
        setupTestGetVariable();
        PowerMockito.doReturn("000").when(ceciSpy, "getVariableFromPage", Mockito.anyInt(), Mockito.anyInt());        
        String screen = "OPTION= LENGTH       LENGTH= H ";
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to parse command output option value");

        ceciSpy.getOptionValue(screen);
    }
    
    @Test
    public void testGetOptionValueInHex() throws Exception {
        setupTestGetVariable();
        String value = "F1F1"; 
        PowerMockito.doReturn(value).when(ceciSpy, "getVariableHexFromPage", Mockito.anyInt(), Mockito.anyInt());
        
        Assert.assertEquals("Error in getOptionValue() method", value, ceciSpy.getOptionValueInHex(1, 0));
        
        Assert.assertEquals("Error in getOptionValue() method", value, ceciSpy.getOptionValueInHex(1, 1));
        
        Assert.assertEquals("Error in getOptionValue() method", value, ceciSpy.getOptionValueInHex(2, 0));
        
        Assert.assertEquals("Error in getOptionValue() method", value + value, ceciSpy.getOptionValueInHex(5, 0));
    }
    
    @Test
    public void testGetOptionValueInHexException1() throws Exception {
        setupTestGetVariable();
        PowerMockito.doReturn("F1").when(ceciSpy, "getVariableHexFromPage", Mockito.anyInt(), Mockito.anyInt());
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to parse command output binary option value");
        
        ceciSpy.getOptionValueInHex(1, 0);
    }
    
    @Test
    public void testGetOptionValueInHexException2() throws Exception {
        setupTestGetVariable();
        PowerMockito.doReturn("F1").when(ceciSpy, "getVariableHexFromPage", Mockito.anyInt(), Mockito.anyInt());
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException()); 
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Unable to parse command output binary option value");
        
        ceciSpy.getOptionValueInHex(1, 0);
    }
}
