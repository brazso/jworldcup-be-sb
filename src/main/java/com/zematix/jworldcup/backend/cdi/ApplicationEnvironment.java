package com.zematix.jworldcup.backend.cdi;

import com.google.common.base.Strings;
import com.zematix.jworldcup.backend.util.CommonUtil;

/**
 * Supported application environments.
 */
public enum ApplicationEnvironment {
	DEVELOPMENT("development"),
	PRODUCTION("production");
	
	public static final String APP_ENV_PARAMETER_NAME = "application.environment";
	
	// used in config properties as file name
	private String bundleName;

	/**
	 * Constructor
	 * 
	 * @param name Used in persistence.xml as unit name 
	 */
	private ApplicationEnvironment(String bundleName) {
		this.bundleName = bundleName;
	}
	
	public String getBundleName() {
		return bundleName;
	}
	
	/**
	 * Retrieves application environment from JVM parameter named "application.environment".
	 * Returns "production" if the parameter does not exist, {@code null} if there is 
	 * unsupported parameter value.
	 * @return {@link ApplicationEnvironment} instance or null not supported parameter 
	 */
	public static ApplicationEnvironment getApplicationEnvironmentFromJVM() {
		ApplicationEnvironment appEnv = null;
		String environment = System.getProperty(APP_ENV_PARAMETER_NAME);
		if (Strings.isNullOrEmpty(environment)) {
			appEnv = ApplicationEnvironment.PRODUCTION; // default
		}
		else {
			appEnv = ApplicationEnvironment.valueOf(environment.toUpperCase());
		}
		return appEnv;
	}

	/**
	 * Due to the different application environments the used {@link EnitityManagerFactory}
	 * name might contain the environment as postfix in the used unit name.
	 * This method adds the environment postfix to the given unit name. 
	 * It returns with the unit name concatenated with environment.
	 * 
	 * @param unitName - unit name
	 * @return unit name concatenated with postix application environment
	 */
	public static String getUnitNameWithEnvironment(String unitName) {
		String unitNameWithEnvironment = unitName.toLowerCase() 
				+ CommonUtil.capitalizeFirstLetter(ApplicationEnvironment.getApplicationEnvironmentFromJVM().getBundleName());
		return unitNameWithEnvironment;
	}
	/**
	 * Due to the different application environments the used {@link EnitityManagerFactory}
	 * unit name might contain the environment as postfix in its name.
	 * This method removes the environment postfix from the given unit name if it exists. 
	 * It returns with the unit name without optional postfix environment.
	 * 
	 * @param unitNameWithEnvironment - unit name containing postfix application environment
	 * @return unit name without postix application environment
	 */
	public static String getUnitNameFromEnvironment(String unitNameWithEnvironment) {
		String unitName = unitNameWithEnvironment; // default
		for (ApplicationEnvironment appEnv : ApplicationEnvironment.values()) {
			String withEnvironment = CommonUtil.capitalizeFirstLetter(appEnv.getBundleName());
			if (unitNameWithEnvironment.endsWith(withEnvironment)) {
				unitName = unitNameWithEnvironment.substring(0, unitNameWithEnvironment.length() - withEnvironment.length());
				break;
			}
		}
		return unitName;
	}
}
