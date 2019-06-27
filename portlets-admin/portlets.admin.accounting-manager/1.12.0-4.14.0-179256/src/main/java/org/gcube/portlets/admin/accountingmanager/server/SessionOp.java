package org.gcube.portlets.admin.accountingmanager.server;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.gcube.portlets.admin.accountingmanager.server.util.ServiceCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
  * @author Giancarlo Panichi
 *
 *
 * @param <T> tpye
 */
public class SessionOp<T> {

	private static Logger logger = LoggerFactory.getLogger(SessionOp.class);

	public T get(HttpServletRequest httpRequest,
			ServiceCredentials serviceCredentials, String attribute,
			Class<T> cls) throws Exception {
		HttpSession httpSession = httpRequest.getSession();
		T value = null;

		@SuppressWarnings("unchecked")
		HashMap<String, T> map = (HashMap<String, T>) httpSession
				.getAttribute(attribute);

		if (map != null) {
			if (map.containsKey(serviceCredentials.getScope())) {
				value = map.get(serviceCredentials.getScope());
			} else {
				logger.info("" + attribute + " was not acquired");
				try {
					value = cls.newInstance();
					map.put(serviceCredentials.getScope(), value);
				} catch (InstantiationException | IllegalAccessException e) {
					String error = "Error reading session attribute: "
							+ e.getLocalizedMessage();
					logger.error(error, e);
					throw new Exception(error, e);
				}

			}
		} else {
			logger.info("" + attribute + " was not acquired");
			map = new HashMap<>();
			try {
				value = cls.newInstance();
				map.put(serviceCredentials.getScope(), value);
				httpSession.setAttribute(attribute, map);
			} catch (InstantiationException | IllegalAccessException e) {
				String error = "Error reading session attribute: "
						+ e.getLocalizedMessage();
				logger.error(error, e);
				throw new Exception(error, e);
			}

		}
		return value;
	}

	public T get(HttpServletRequest httpRequest,
			ServiceCredentials serviceCredentials, String attribute) {
		HttpSession httpSession = httpRequest.getSession();
		T value = null;

		@SuppressWarnings("unchecked")
		HashMap<String, T> map = (HashMap<String, T>) httpSession
				.getAttribute(attribute);

		if (map != null) {
			if (map.containsKey(serviceCredentials.getScope())) {
				value = map.get(serviceCredentials.getScope());
			} else {
				logger.info("" + attribute + " was not acquired");
			}
		} else {
			logger.info("" + attribute + " was not acquired");
		}
		return value;
	}

	public void set(HttpServletRequest httpRequest,
			ServiceCredentials serviceCredentials, String attribute, T value) {
		HttpSession httpSession = httpRequest.getSession();

		@SuppressWarnings("unchecked")
		HashMap<String, T> map = (HashMap<String, T>) httpSession
				.getAttribute(attribute);

		if (map != null) {
			map.put(serviceCredentials.getScope(), value);
		} else {
			map = new HashMap<>();
			map.put(serviceCredentials.getScope(), value);
			httpSession.setAttribute(attribute, map);
		}
	}

	public void remove(HttpServletRequest httpRequest,
			ServiceCredentials serviceCredentials, String attribute) {
		HttpSession httpSession = httpRequest.getSession();

		@SuppressWarnings("unchecked")
		HashMap<String, T> map = (HashMap<String, T>) httpSession
				.getAttribute(attribute);

		if (map != null) {
			map.remove(serviceCredentials.getScope());
		}
	}

}
