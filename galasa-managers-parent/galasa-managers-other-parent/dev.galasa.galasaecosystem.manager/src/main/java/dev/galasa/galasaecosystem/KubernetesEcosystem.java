package dev.galasa.galasaecosystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.galasaecosystem.internal.GalasaEcosystemManagerField;

/**
 * Kubernetes Ecosystem
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}KubernetesEcosystem</code> annotation will request the Galasa Ecosystem Manager to provision
 * a Galasa Ecosystem within a Kubernetes Namespace.
 * 
 * @galasa.examples 
 * <code>{@literal @}KubernetesEcosystem<br>
 * public IKubernetesEcosystem ecosystem;<br>
 * <br>
 * {@literal @}KubernetesEcosystem(yamlDirectory="/k8syaml"<br>
 * public IKubernetesEcosystem ecosystem;<br>
 * </code>
 * 
 * @galasa.extra
 * The <code>IKubernetesEcosystem</code> interface gives the test access to the URLs to all the services and API endpoints within the Ecosystem.
 * When the test starts to run, all the services will be up and verified.<br>
 * <br>
 * The Galasa Ecosystem has it's own stable versions of the Kubernetes YAML files necessary to create the entire Ecosystem.  If you wish to override those and
 * use your own yaml files,  then use the yamlDirectory attribute.  If a resource is missing in the test's set,  then the stable version will be used. 
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ IKubernetesEcosystem.class })
@GalasaEcosystemManagerField
public @interface KubernetesEcosystem {

    /**
     * The <code>ecosystemNamespaceTag</code> is used to identify the Ecosystem to other Managers or Shared Environments.  If a test is using multiple 
     * Ecosystems, each separate Ecosystem must have a unique tag.  If two Ecosystems use the same tag, they will refer to the 
     * same actual Ecosystem.
     */
    public String ecosystemNamespaceTag() default "primary";
    
    /**
     * The <code>kubernetesNamespaceTag</code> to identify which tagged Kubernetes Namespace is to be used to deploy the Galasa Ecosystem into.
     */
    public String kubernetesNamespaceTag() default "primary";
    
    /**
     * The <code>yamlDirectory</code> points to a resource directory within the test bundle that contains a set of override yaml files to use when creating the 
     * Ecosystem.  Each file must end with .yaml to be found.  If a directory or resource is not provided, the stable yaml files within the Ecosystem Manager will be used.
     */
    public String yamlDirectory() default "";
}