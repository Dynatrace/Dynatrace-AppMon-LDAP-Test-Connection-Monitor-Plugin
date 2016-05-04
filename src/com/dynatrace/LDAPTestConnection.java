
 /**
  * LDAP Test Connection Monitor Plugin
  * Author: Mélory Zolino - Guardian Delivery Consultant at Dynatrace
  * Date: 23/01/2015 (DD/MM/AAAA)
  * 
  * Description: This plugin was developed to monitor ...
  **/ 

package com.dynatrace;

import com.dynatrace.diagnostics.pdk.*;

import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;


public class LDAPTestConnection implements Monitor {
	
	//input variables
	private static final String PROTOCOL = "PROTOCOL";
	private static final String PORT_NUMBER = "PORT_NUMBER";
	private static final String SECURITY_PRINCIPAL = "SECURITY_PRINCIPAL";
	private static final String SECURITY_CREDENTIALS = "SECURITY_CREDENTIALS";
	private static final String TIMEOUT = "TIMEOUT";
	
	//metrics group
	private static final String METRIC_GROUP = "LDAP Test Connection";
	private static final String MSG_RESPONSE_TIME = "ResponseTime";
	private static final String MSG_CONNECTION_STATUS = "ConnectionStatus";
	
	//global variable
	private static final double MILLIS = 0.000001;
	private static final Logger log = Logger.getLogger(LDAPTestConnection.class.getName());
	long time;
	
	@Override
	public Status setup(MonitorEnvironment env) throws Exception {
		time = 0;
		return new Status(Status.StatusCode.Success);
	}

	@Override
	public Status execute(MonitorEnvironment env) throws Exception {
		Status status = new Status();
		Properties authEnv = new Properties();
		Collection<MonitorMeasure> measures;
		
		int connectionStatus = 0;
		long responseTime = 0;
		double responseTimeMillis = 0;
			
		String ldapURL = env.getConfigString(PROTOCOL) + "://" + env.getHost().getAddress() + ":" + env.getConfigLong(PORT_NUMBER);
		
		log.info("Plugin execution initialized.");
		log.info (ldapURL);
		
		authEnv.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
		authEnv.put("com.sun.jndi.ldap.connect.timeout", env.getConfigString(TIMEOUT));
		authEnv.put(Context.PROVIDER_URL, ldapURL);
		authEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
        authEnv.put(Context.SECURITY_PRINCIPAL, env.getConfigString(SECURITY_PRINCIPAL));
        authEnv.put(Context.SECURITY_CREDENTIALS, env.getConfigPassword(SECURITY_CREDENTIALS));
                
        time = System.nanoTime();  
        try {	
            DirContext authContext = new InitialDirContext(authEnv);
            responseTime = System.nanoTime() - time;
            connectionStatus = 1;
            authContext.close();            
           
            responseTimeMillis = responseTime * MILLIS;
            measures = env.getMonitorMeasures(METRIC_GROUP, MSG_RESPONSE_TIME);
				for (MonitorMeasure measure : measures)
					measure.setValue(responseTimeMillis);
				
			measures = env.getMonitorMeasures(METRIC_GROUP, MSG_CONNECTION_STATUS);
				for (MonitorMeasure measure : measures)
					measure.setValue(connectionStatus);
			
			/*	
			log.info ("Collected metrics:");
			log.info ("ConnectionStatus: " + connectionStatus);
			log.info ("ResponseTime: " + responseTimeMillis);
			*/
			return new Status(Status.StatusCode.Success);
        } catch (NamingException e) {      	
        	
        	connectionStatus=0;
        	responseTime = System.nanoTime() - time;
        	
        	responseTimeMillis = responseTime * MILLIS;
        	measures = env.getMonitorMeasures(METRIC_GROUP, MSG_RESPONSE_TIME);
			for (MonitorMeasure measure : measures)
				measure.setValue(responseTimeMillis);
			
			measures = env.getMonitorMeasures(METRIC_GROUP, MSG_CONNECTION_STATUS);
			for (MonitorMeasure measure : measures)
				measure.setValue(connectionStatus);
			log.severe ("-----------------------------------------");
			log.severe ("Collected metrics:");
			log.severe ("ConnectionStatus: " + connectionStatus);
			log.severe ("ResponseTime: " + responseTimeMillis);
			log.severe ("Error:" + e.getMessage());
			log.severe ("-----------------------------------------");
			
			status.setException(e);
			status.setStatusCode(Status.StatusCode.PartialSuccess);
			status.setShortMessage(e.getClass().getSimpleName());
			status.setMessage(e.getMessage());
					
        	return status;
        } 
		
	}


	@Override
	public void teardown(MonitorEnvironment env) throws Exception {
		// TODO
	}
}
