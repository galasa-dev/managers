package dev.galasa.elastic.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.RestHighLevelClient;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.elastic.ElasticManagerException;
import dev.galasa.elastic.IElasticManager;
import dev.galasa.elastic.internal.properties.ElasticEndpoint;
import dev.galasa.elastic.internal.properties.ElasticPropertiesSingleton;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;

@Component(service = { IManager.class })
public class ElasticManagerImpl extends AbstractManager implements IElasticManager {

    private static final Log                    logger          = LogFactory.getLog(ElasticManagerImpl.class);
    private RestHighLevelClient                 elasticClient;
    public final static String                  NAMESPACE       = "elastic";
    private IConfigurationPropertyStoreService  cps;
    private IDynamicStatusStoreService          dss;

    private String                              testCase;
    private Date                                startTimestamp;
    private Date                                endTimestamp;

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);
        List<AnnotatedField> ourFields = findAnnotatedFields(ElasticManagerField.class);
        if (!ourFields.isEmpty()) {
            youAreRequired(allManagers, activeManagers);
        }

        try {
            this.cps = framework.getConfigurationPropertyService(NAMESPACE);
            this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
            this.testCase = framework.getTestRunName();
            ElasticPropertiesSingleton.setCps(cps);
        } catch (Exception e) {
            throw new ElasticManagerException("Unable to request framework services", e);
        }
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
            throws ManagerException {
        if (activeManagers.contains(this))
            return;
        activeManagers.add(this);
    }

    @Override
    public void provisionBuild() throws ManagerException, ResourceUnavailableException {
        this.elasticClient = ElasticClientBuilder.buildClient(ElasticEndpoint.get());
    }

    public void startOfTestClass() throws ManagerException {
        this.startTimestamp = new Date();
    }

    public String endOfTestClass(@NotNull String currentResult, Throwable currentException) throws ManagerException {
        this.endTimestamp = new Date();
        return null;
    }

    @Override
    public void testClassResult(@NotNull String finalResult, Throwable finalException) throws ManagerException {
        //Send result
        logger.info("Sending info to Elastic Search");
    }
}