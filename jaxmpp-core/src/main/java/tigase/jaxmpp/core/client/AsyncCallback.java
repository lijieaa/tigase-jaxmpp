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
package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 * Main interface for callback of all <a
 * href='http://xmpp.org/rfcs/rfc6120.html#stanzas-semantics-iq'>IQ</a>
 * asynchronous request-response mechanism.
 * 
 * @author bmalkow
 * 
 */
public interface AsyncCallback {

	/**
	 * Called when received response has type {@linkplain StanzaType#error
	 * error}.
	 * 
	 * @param responseStanza
	 *            received IQ stanza
	 * @param error
	 *            error condition
	 * @throws JaxmppException
	 */
	void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException;

	/**
	 * Called when received response has type {@linkplain StanzaType#result
	 * result}.
	 * 
	 * @param responseStanza
	 *            received stanza
	 * @throws JaxmppException
	 */
	void onSuccess(Stanza responseStanza) throws JaxmppException;

	/**
	 * Called when response wasn't received in given time.
	 * 
	 * @throws JaxmppException
	 */
	void onTimeout() throws JaxmppException;

}