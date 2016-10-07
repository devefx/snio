package org.devefx.snio.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class StringManager {
	private ResourceBundle bundle;
	private static Logger log = LoggerFactory.getLogger(StringManager.class);
	private static Hashtable<String, StringManager> managers = new Hashtable<>();
	
	private StringManager(String packageName) {
		String bundleName = packageName + ".LocalStrings";
		
		try {
			bundle = ResourceBundle.getBundle(bundleName);
		} catch (MissingResourceException e) {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			if (loader != null) {
				try {
					bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault(), loader);
					return;
				} catch (Exception ex) {
				}
			} else {
				loader = getClass().getClassLoader();
			}
			
			if (log.isDebugEnabled()) {
				log.debug("Can\'t find resource " + bundleName + " " + loader);
			}
		}
	}
	
	public String getString(String key) {
		return MessageFormat.format(getStringInternal(key), (Object[]) null);
	}
	
	protected String getStringInternal(String key) {
		if (key == null) {
			throw new NullPointerException("key is null");
		}
		if (bundle == null) {
			return null;
		}
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return "Cannot find message associated with key \'" + key + "\'";
		}
	}
	
	public String getString(String key, Object ...args) {
		String value = getStringInternal(key);
		try {
			Object[] iae = args;
			for (int i = 0; i < args.length; i++) {
				if (args[i] == null) {
					if (iae == args) {
						iae = args.clone();
					}
					iae[i] = "null";
				}
			}
			return MessageFormat.format(value, iae);
		} catch (IllegalArgumentException e) {
			StringBuffer buf = new StringBuffer();
			buf.append(value);
			for(int i = 0; i < args.length; ++i) {
				buf.append(" arg[" + i + "]=" + args[i]);
			}
			return buf.toString();
		}
	}
	
	public static synchronized StringManager getManager(String packageName) {
		StringManager manager = managers.get(packageName);
		if (manager == null) {
			manager = new StringManager(packageName);
			managers.put(packageName, manager);
		}
		return manager;
	}
}
