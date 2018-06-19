/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.jaxmpp.core.client.factory;

import java.util.HashMap;

/**
 * Provides methods to inject alternative implementation of object in several
 * points in this library.
 * 
 * @author bmalkow
 * 
 */
public class UniversalFactory {

	/**
	 * Interface for object factory.
	 * 
	 * @param <T>
	 *            type of created objects.
	 */
	public static interface FactorySpi<T> {

		/**
		 * Creates new instance of class.
		 * 
		 * @return
		 */
		T create();
	}

	private static UniversalFactory instance;

	/**
	 * Creates instance of object with given name.
	 * 
	 * @param key
	 *            name of object.
	 * @return instance of object or <code>null</code> if no factory is
	 *         registered for that name.
	 */
	public static <T> T createInstance(String key) {
		@SuppressWarnings("unchecked")
		FactorySpi<T> spi = (FactorySpi<T>) instance().factories.get(key);
		if (spi == null)
			return null;
		return spi.create();
	}

	static UniversalFactory instance() {
		if (instance == null)
			instance = new UniversalFactory();
		return instance;
	}

	/**
	 * Registers object factory with name. Previously registered factory will
	 * not be overwritten.
	 * 
	 * @param key
	 *            name of object factory.
	 * @param spi
	 *            implementation of factory.
	 */
	public static void setSpi(String key, FactorySpi<?> spi) {
		setSpi(key, spi, false);
	}

	/**
	 * Registers object factory with name.
	 * 
	 * @param key
	 *            name of object factory.
	 * @param spi
	 *            implementation of factory.
	 * @param overwrite
	 *            <code>true</code> if previously registered factory should be
	 *            overwritten.
	 */
	public static void setSpi(String key, FactorySpi<?> spi, boolean overwrite) {
		if (!overwrite && instance().factories.containsKey(key))
			return;
		instance().factories.put(key, spi);
	}

	private final HashMap<String, FactorySpi<?>> factories = new HashMap<String, FactorySpi<?>>();

	private UniversalFactory() {
	}

}