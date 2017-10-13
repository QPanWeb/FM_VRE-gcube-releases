package org.gcube.common.utils.encryption;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * A simplified keys generator for the most common algorithms
 * @author Manuele Simi (CNR)
 *
 */
public class KeyFactory {

	static org.apache.commons.logging.Log log = 
	        org.apache.commons.logging.LogFactory.getLog(
	        		KeyFactory.class.getName());

	static {
        org.apache.xml.security.Init.init();
    }
	/**
     * Generates an AES key
     */
    protected static SecretKey newAESKey() throws Exception {
       	KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }
    
    /**
     * Generates a TripleDES key
     */
    protected static SecretKey newTripleDESKey() throws Exception {
    	KeyGenerator keyGenerator = KeyGenerator.getInstance("TripleDES");
        //keyGenerator.init(168);
        return keyGenerator.generateKey();
    }
    
    /**
     * Generates a Rijndael key
     */
    protected static SecretKey newRijndaelKey() throws Exception {
    	KeyGenerator keyGenerator = KeyGenerator.getInstance("Rijndael");
        //keyGenerator.init(168);
        return keyGenerator.generateKey();
    }
    
    /**
     * Generates a DESede key
     */
    protected static SecretKey newDESKey() throws Exception {
    	KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
        //keyGenerator.init(168);
        return keyGenerator.generateKey();
    }
    
}
