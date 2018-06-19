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
package tigase.jaxmpp.core.client.xmpp.forms;

import java.util.ArrayList;
import java.util.List;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Implementation of text-multi field type.
 * <p>
 * <blockquote
 * cite='http://xmpp.org/extensions/xep-0004.html#protocol-formtypes'>The field
 * enables an entity to gather or provide multiple lines of text.</blockquote>
 * </p>
 */
public class TextMultiField extends AbstractField<String[]> {

	TextMultiField(Element element) throws XMLException {
		super("text-multi", element);
	}

	/**
	 * Adds value to field.
	 * 
	 * @param value
	 *            value to add
	 */
	public void addFieldValue(String... value) throws XMLException {
		if (value != null)
			for (String string : value) {
				addChild(ElementFactory.create("value", string, null));
			}
	}

	/**
	 * Removes all values.
	 */
	public void clearValues() throws XMLException {
		List<Element> lls = getChildren("value");
		if (lls != null)
			for (Element element : lls) {
				removeChild(element);
			}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getFieldValue() throws XMLException {
		ArrayList<String> result = new ArrayList<String>();
		List<Element> lls = getChildren("value");
		if (lls != null)
			for (Element element : lls) {
				result.add(element.getValue());
			}
		return result.toArray(new String[] {});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFieldValue(String[] value) throws XMLException {
		clearValues();
		if (value != null) {
			for (String string : value) {
				addChild(ElementFactory.create("value", string, null));
			}
		}
	}

}