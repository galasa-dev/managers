package dev.galasa.common.zos.internal.properties;

import dev.galasa.common.zos.ZosManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Extra bundle to required to implement the zOS Console Manager
 * <p>
 * The name of the Bundle that implements the zOS Console Manager 
 * </p><p>
 * The property is:<br>
 * {@code zos.bundle.extra.console.manager=dev.galasa.common.zosconsole.zosmf.manager} 
 * </p>
 * <p>
 * The default value is {@value #DEFAULT_BUNDLE_EXTRA_CONSOLE_MANAGER}
 * </p>
 *
 */
public class ConsoleExtraBundle extends CpsProperties {
	
	private static final String DEFAULT_BUNDLE_EXTRA_CONSOLE_MANAGER = "dev.galasa.common.zosconsole.zosmf.manager";
	
	public static String get() throws ZosManagerException {
		try {
			String bundleName = getStringNulled(ZosPropertiesSingleton.cps(), "bundle.extra", "console.manager");
			if (bundleName == null)  {
				return DEFAULT_BUNDLE_EXTRA_CONSOLE_MANAGER;
			}
			return bundleName;
		} catch (ConfigurationPropertyStoreException e) {
			throw new ZosManagerException("Problem asking CPS for the console manager extra bundle name", e); 
		}
	}
}
