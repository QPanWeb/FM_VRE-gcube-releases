package it.eng.rdlab.soa3.assertion.configuration;


/**
 * 
 * Configuration Bean
 * 
 * @author Ciro Formisano
 *
 */
public interface ConfigurationBean 
{

	public static final String PUBLIC_KEY = "public.key";
	public static final String KEYFACTORY_ALG = "RSA";
	public static final String CERTIFICATE_TYPE = "X.509";
	public static final String SIGNATURE_VALIDATION_ENABLED = "sign.validation.en";
	public static final String TIME_VALIDATION_ENABLED = "time.validation.en";
	
	/**
	 * 
	 * Provides the configuration property
	 * 
	 * @param propertyname
	 * @return the property, if exists, null otherwise
	 */
	public String getProperty(String propertyname);

}
