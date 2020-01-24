package dev.galasa.kubernetes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.kubernetes.internal.KubernetesManagerField;

/**
 * Kubernetes Namespace
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}KubernetesNamespace</code> annotation will request the Kubernetes Manager allocate a namespace
 * on the infrastructure Kubernetes Clusters.  The test can request as many Namesapces as required that 
 * can be supported simultaneously by the Kubernetes Manager configuration.
 * 
 * @galasa.examples 
 * <code>{@literal @}KubernetesNamespace<br>
 * public IKubernetesNamesapce namespace;<br>
 * </code>
 * 
 * @galasa.extra
 * The <code>IKubernetesNamespace</code> interface gives the test access to create and manage resources on the Kubernetes cluster. 
 * See {@link KubernetesNamespace} and {@link IKubernetesNamespace} to find out more.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ IKubernetesNamespace.class })
@KubernetesManagerField
public @interface KubernetesNamespace {

    /**
     * The <code>tag</code> is used to identify the Kubernetes names to other Managers or Shared Environments.  If a test is using multiple 
     * Kubernetes Namespace, each separate Kubernetes Namesapce must have a unique tag.  If two Kubernetes Namespace use the same tag, they will refer to the 
     * same actual Kubernetes Namespace.
     */
    public String kubernetesNamespaceTag() default "primary";
}