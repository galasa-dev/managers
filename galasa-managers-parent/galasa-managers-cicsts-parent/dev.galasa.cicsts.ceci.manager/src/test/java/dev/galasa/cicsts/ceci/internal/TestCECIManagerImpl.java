package dev.galasa.cicsts.ceci.internal;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

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

import dev.galasa.ManagerException;
import dev.galasa.cicsts.ceci.CECIException;
import dev.galasa.cicsts.ceci.CECIManagerException;
import dev.galasa.cicsts.ceci.internal.properties.CECIPropertiesSingleton;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zos3270.ITerminal;

@RunWith(PowerMockRunner.class)
public class TestCECIManagerImpl {
    
    private CECIManagerImpl ceciManager;
    
    private CECIManagerImpl ceciManagerSpy;
    
    private List<IManager> allManagers;
    
    private List<IManager> activeManagers;
    
    @Mock
    private IFramework frameworkMock;
    
    @Mock
    public IManager managerMock;
    
    @Mock
    private ZosManagerImpl zosManagerMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Before
    public void setup() throws Exception {
        CECIPropertiesSingleton ceciPropertiesSingleton = new CECIPropertiesSingleton();
        ceciPropertiesSingleton.activate();
        
        ceciManager = new CECIManagerImpl();
        ceciManagerSpy = Mockito.spy(ceciManager);
        Mockito.when(ceciManagerSpy.getFramework()).thenReturn(frameworkMock);
        
        allManagers = new ArrayList<>();
        activeManagers = new ArrayList<>();
    }

    @Test
    public void testInitialise() throws ManagerException, ConfigurationPropertyStoreException {        
        allManagers.add(managerMock);
        ceciManager.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestCECIManagerImpl.class));
        Assert.assertEquals("Error in initialise() method", ceciManagerSpy.getFramework(), frameworkMock);
        
        ceciManager.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
        Assert.assertEquals("Error in initialise() method", ceciManagerSpy.getFramework(), frameworkMock);
        
        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
        exceptionRule.expect(CECIManagerException.class);
        exceptionRule.expectMessage("Unable to request framework services");
        ceciManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
    }

    @Test
    public void testYouAreRequired() throws Exception {
        allManagers.add(zosManagerMock);
        ceciManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(ceciManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(ceciManagerSpy);
        ceciManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(ceciManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testProvisionGenerate() throws Exception {
        PowerMockito.doNothing().when(ceciManagerSpy, "generateAnnotatedFields", Mockito.any());
        ceciManagerSpy.provisionGenerate();
        PowerMockito.verifyPrivate(ceciManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
    }

    @Test
    public void testGenerateCECI() throws NoSuchFieldException, SecurityException {
        List<Annotation> annotations = new ArrayList<>();
        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosconsole.ZosConsole.class);
        annotations.add(annotation);
        
        Object zosConsoleImplObject = ceciManager.generateCECI(DummyTestClass.class.getDeclaredField("ceci"), annotations);
        Assert.assertTrue("Error in generateZosConsole() method", zosConsoleImplObject instanceof CECIImpl);
    }
    
    
    class DummyTestClass {
        @dev.galasa.cicsts.ceci.CECI
        public dev.galasa.cicsts.ceci.ICECI ceci;
        @Mock
        public ITerminal ceciTerminal;
        @dev.galasa.Test
        public void dummyTestMethod() throws CECIException {
            ceci.issueCommand(ceciTerminal, "command");
        }
    }

}
