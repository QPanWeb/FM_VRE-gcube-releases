package it.eng.rdlab.um.ldap.validators;

import it.eng.rdlab.um.ldap.Utils;
import it.eng.rdlab.um.ldap.role.bean.LdapRoleModel;


import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LdapRoleValidator 
{
	public static boolean validate(String dn, List<String> objectClassesList, Map<String, String> attributeMap, List<String> roleOccupantList) 
	{
		Log log = LogFactory.getLog(LdapRoleValidator.class);
		log.debug("Checking object classes list...");

		if (dn==null || (Utils.parseDN(dn))==null)
		{
			log.debug("Distinguished name not valid");
			return false;
		}
		else if (objectClassesList ==null || objectClassesList.size() == 0 || !objectClassesList.contains(LdapRoleModel.OBJECT_CLASS_ROLE))
		{
			log.debug("Invalid object class list");
			return false;
		}
		else
		{
			log.debug("Checking user mandatory parameters dn, cn and sn");
			log.debug("DN = "+dn);
			boolean cn = attributeMap.get(LdapRoleModel.ROLE_CN) != null;
			log.debug("Common name ok = "+cn);
			return (cn && roleOccupantList != null && roleOccupantList.size()>0);
		}
	}



}
