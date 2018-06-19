/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc. <office@tigase.com>
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
package tigase.jaxmpp.j2se.connectors.socket;

import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.j2se.xml.J2seElement;
import tigase.xml.SimpleParser;
import tigase.xml.SingletonFactory;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tigase.jaxmpp.j2se.connectors.socket.SocketConnector.DEFAULT_SOCKET_BUFFER_SIZE;

/**
 *
 * @author andrzej
 */
public abstract class Worker extends Thread {

	private final char[] buffer = new char[DEFAULT_SOCKET_BUFFER_SIZE];

	private final Connector connector;

	private final XMPPDomBuilderHandler domHandler = new XMPPDomBuilderHandler(new StreamListener() {

		@Override
		public void nextElement(tigase.xml.Element element) {
			try {
				try {
					processElement(new J2seElement(element));
				} catch (JaxmppException e) {
					onErrorInThread(e);
				}
			} catch (JaxmppException e) {
				log.log(Level.SEVERE, "Error on processing element", e);
			}
		}

		@Override
		public void xmppStreamClosed() {
			try {
				if (log.isLoggable(Level.FINEST)) {
					log.finest("xmppStreamClosed()");
				}
				onStreamTerminate();
			} catch (JaxmppException e) {
				log.log(Level.WARNING, "Error on processing Stream Closed", e);
			}
		}

		@Override
		public void xmppStreamOpened(Map<String, String> attribs) {
			if (log.isLoggable(Level.FINEST)) {
				log.finest("xmppStreamOpened()");
			}
			onStreamStart(attribs);
		}
	});

	private final Logger log = Logger.getLogger(Worker.class.getCanonicalName());

	private final SimpleParser parser = SingletonFactory.getParserInstance();

	public Worker(Connector connector) {
		this.connector = connector;
	}

	protected abstract Reader getReader();

	@Override
	public void interrupt() {
		super.interrupt();
		log.log(Level.FINE, "Worker Interrupted");
	}

	protected abstract void onErrorInThread(Exception e) throws JaxmppException;

	protected abstract void onStreamStart(Map<String, String> attribs);

	protected abstract void onStreamTerminate() throws JaxmppException;

	protected abstract void processElement(Element elem) throws JaxmppException;

	@Override
	public void run() {
		super.run();
		log.finest(hashCode() + " Starting " + this);

		int r = -2;
		try {
			Reader reader;// = getReader();
			while ((reader = getReader()) != null && !isInterrupted() && (r = reader.read(buffer)) != -1
					&& connector.getState() != Connector.State.disconnected) {
				log.info("Received:"+new String(buffer));
				parser.parse(domHandler, buffer, 0, r);
			}
			// if (log.isLoggable(Level.FINEST))
			log.finest(hashCode() + "Disconnecting: state=" + connector.getState() + "; buffer=" + r + "   " + this);
			if (!isInterrupted()) {
				onStreamTerminate();
			}
		} catch (Exception e) {
			if (connector.getState() != Connector.State.disconnecting && connector.getState() != Connector.State.disconnected) {
				log.log(Level.WARNING, "Exception in worker", e);
				try {
					onErrorInThread(e);
				} catch (JaxmppException e1) {
					log.log(Level.WARNING, "Error on handling another exception", e);
				}
			}
		} finally {
			interrupt();
			log.finest("Worker2 is interrupted");
			workerTerminated();
		}
	}

	protected abstract void workerTerminated();
}
