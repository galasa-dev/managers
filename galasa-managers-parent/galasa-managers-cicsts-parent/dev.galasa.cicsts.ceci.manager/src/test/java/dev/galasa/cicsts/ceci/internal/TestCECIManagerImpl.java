package dev.galasa.cicsts.ceci.internal;

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
import dev.galasa.cicsts.CeciManagerException;
import dev.galasa.cicsts.ceci.internal.properties.CECIPropertiesSingleton;
import dev.galasa.cicsts.internal.CicstsManagerImpl;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.internal.ZosManagerImpl;

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
    
    @Mock
    private CicstsManagerImpl cicsManagerMock;

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
        allManagers.add(cicsManagerMock);
        ceciManager.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestCECIManagerImpl.class));
        Assert.assertEquals("Error in initialise() method", ceciManagerSpy.getFramework(), frameworkMock);
        
        ceciManager.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
        Assert.assertEquals("Error in initialise() method", ceciManagerSpy.getFramework(), frameworkMock);
        
        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
        exceptionRule.expect(CeciManagerException.class);
        exceptionRule.expectMessage("Unable to request framework services");
        ceciManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
    }

    class DummyTestClass {
    }

}
