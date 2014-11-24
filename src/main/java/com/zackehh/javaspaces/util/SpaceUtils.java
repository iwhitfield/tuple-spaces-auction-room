package com.zackehh.javaspaces.util;

import net.jini.space.JavaSpace;
import net.jini.core.transaction.server.TransactionManager;
import java.rmi.RMISecurityManager;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;

public class SpaceUtils {

	public static JavaSpace getSpace(String hostname) {
		JavaSpace js = null;
		try {
			LookupLocator l = new LookupLocator("jini://" + hostname);

			ServiceRegistrar sr = l.getRegistrar();

			Class[] classTemplate = {
                Class.forName("net.jini.space.JavaSpace")
            };

			js = (JavaSpace) sr.lookup(new ServiceTemplate(null, classTemplate, null));
            
            System.out.println("Successfully connected!");
		} catch (Exception e) {
			System.err.println("Error: " + e);
		}
		return js;
	}

	public static JavaSpace getSpace() {
		return getSpace("localhost");
	}

	public static TransactionManager getManager(String hostname) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		TransactionManager tm = null;
		try {
			LookupLocator l = new LookupLocator("jini://" + hostname);

			ServiceRegistrar sr = l.getRegistrar();

			Class[] classTemplate = {
                Class.forName("net.jini.core.transaction.server.TransactionManager")
            };

			tm = (TransactionManager) sr.lookup(new ServiceTemplate(null, classTemplate, null));
		} catch (Exception e) {
			System.err.println("Error: " + e);
		}
		return tm;
	}

	public static TransactionManager getManager() {
		return getManager("waterloo");
	}
}

