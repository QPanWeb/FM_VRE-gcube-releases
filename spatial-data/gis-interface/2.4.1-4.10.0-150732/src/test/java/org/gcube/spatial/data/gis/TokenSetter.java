package org.gcube.spatial.data.gis;

import java.util.Properties;

import org.gcube.common.authorization.library.provider.SecurityTokenProvider;
import org.gcube.common.scope.api.ScopeProvider;

public class TokenSetter {

	
	
	private static Properties props=new Properties();
	
	static{
		try {
			props.load(TokenSetter.class.getResourceAsStream("/tokens.properties"));
		} catch (Exception e) {
			throw new RuntimeException("YOU NEED TO SET TOKEN FILE IN CONFIGURATION");
		}
	}
	

	public static void set(String scope){
		try{
			if(!props.containsKey(scope)) throw new RuntimeException("No token found for scope : "+scope);
			SecurityTokenProvider.instance.set(props.getProperty(scope));
		}catch(Throwable e){
		}
		ScopeProvider.instance.set(scope);
	}
	
	
}