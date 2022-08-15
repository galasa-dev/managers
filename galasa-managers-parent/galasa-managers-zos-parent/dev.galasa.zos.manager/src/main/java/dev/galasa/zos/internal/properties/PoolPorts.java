/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.zos.internal.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;

public class PoolPorts extends CpsProperties {

	public static @NotNull List<String> get(String image) throws ZosManagerException {
		return getStringListWithDefault(ZosPropertiesSingleton.cps(), "default", "image", "ports", image);
	}
}
