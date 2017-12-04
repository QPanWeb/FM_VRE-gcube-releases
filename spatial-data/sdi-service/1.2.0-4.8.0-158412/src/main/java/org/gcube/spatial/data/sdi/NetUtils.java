package org.gcube.spatial.data.sdi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetUtils {

	public static boolean isSameHost(String toTestHost,String toLookForHost) throws UnknownHostException {
		log.debug("Checking same hosts {},{}",toTestHost,toLookForHost);
		if(toTestHost.equalsIgnoreCase(toLookForHost)) return true;
		else {
			InetAddress[] toTestHostIPs=InetAddress.getAllByName(toTestHost);
			InetAddress[] toLookForHostIPs=InetAddress.getAllByName(toLookForHost);
			log.debug("Checking IPs. ToTestIPs {}, ToLookForIPs {} ",toTestHostIPs,toLookForHostIPs);
			for(InetAddress toTestIP:toTestHostIPs) {
				for(InetAddress toLookForIP:toLookForHostIPs)
					if(toTestIP.equals(toLookForIP)) return true;
			}
		}
		log.debug("HOSTS are different.");
		return false;
	}
	
	public static String getHostByURL(String url){		
		try{
			return new URL(url).getHost();
		}catch(MalformedURLException e) {
			log.debug("Passed url {} is invalid. Assuming it's an hostname.");
			return url;
		}
	}
	
	public static final String getHost(String endpoint) throws MalformedURLException{
		log.debug("Get host from endpoint {} ",endpoint);
		if(endpoint.startsWith("http")){
			log.debug("Endpoint seems url..");
			return getHostByURL(endpoint);
		}
		return endpoint;
	}
	
	
	public static boolean isUp(String url) throws IOException {
		String finalUrl=resolveRedirects(url);
		log.debug("Checking {} availability .. ",finalUrl);
		URL urlObj=new URL(finalUrl);
		HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
		int status=connection.getResponseCode();
		log.trace("HTTP Status response code for {} is {} ",finalUrl,status);
		return status>=200&&status<300;		
	}
	
	
	public static String resolveRedirects(String url) throws IOException{
		log.debug("Resolving redirect for url {} ",url);
		URL urlObj=new URL(url);
		HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
		int status=connection.getResponseCode();
		if(status>=300&&status<400){
			String newUrl=connection.getHeaderField("Location");
			log.debug("Following redirect from {} to {} ",url,newUrl);
			return resolveRedirects(newUrl);
		}else return url;
	}
}
