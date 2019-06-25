package dev.voras.common.openstack.manager.internal.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.LogFactory;

import dev.voras.common.linux.LinuxImage;
import dev.voras.common.openstack.manager.OpenstackManagerException;
import dev.voras.framework.spi.ConfigurationPropertyStoreException;
import dev.voras.framework.spi.cps.CpsProperties;

/**
 * OpenStack Image Capabilities
 * <p>
 * A comma separated list of what capabilities a image has.  This is installation specific and freeform. 
 * </p><p>
 * The property is:-<br><br>
 * openstack.linux.image.[imagename].capabilities=java,kubectl,git<br>
 * Where imagename is that provided in {@link LinuxImages}<br>
 * In the above example, it is indicating the image has java, kubectl and git installed ready for the test.
 * The test can request these capabilities via {@link LinuxImage}
 * </p>
 * <p>
 * The default is no capabilities
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class LinuxImageCapabilities extends CpsProperties {
	
	public static @NotNull List<String> get(
			@NotNull String image) 
					throws ConfigurationPropertyStoreException, OpenstackManagerException {
		
		return getStringList(OpenstackPropertiesSingleton.cps(), 
	               LogFactory.getLog(LinuxImageCapabilities.class), 
	               "linux.image." + image, 
	               "capabilities");
		
		
		
	}

}
