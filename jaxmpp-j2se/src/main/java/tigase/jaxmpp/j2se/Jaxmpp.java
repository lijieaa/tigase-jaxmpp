/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
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
package tigase.jaxmpp.j2se;

import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.JaxmppCore.LoggedInHandler.LoggedInEvent;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XmppSessionLogic.SessionListener;
import tigase.jaxmpp.core.client.connector.ConnectorWrapper;
import tigase.jaxmpp.core.client.connector.StreamError;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslModule;
import tigase.jaxmpp.core.client.xmpp.modules.streammng.StreamManagementModule;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;
import tigase.jaxmpp.j2se.connectors.bosh.BoshConnector;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;
import tigase.jaxmpp.j2se.connectors.websocket.WebSocketConnector;
import tigase.jaxmpp.j2se.eventbus.ThreadSafeEventBus;
import tigase.jaxmpp.j2se.xmpp.modules.auth.saslmechanisms.ExternalMechanism;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.logging.Level;

/**
 * Main library class for using in standalone, Android and other J2SE compatible
 * application.
 */
public class Jaxmpp extends JaxmppCore {

	public static final String CONNECTOR_TYPE = "connectorType";
	public static final String EXCEPTION_KEY = "jaxmpp#ThrowedException";
	public static final String LOGIN_TIMEOUT_KEY = "LOGIN_TIMEOUT_KEY";
	public static final String SYNCHRONIZED_MODE = "jaxmpp#synchronized";
	private static final Executor DEFAULT_EXECUTOR = new Executor() {

		@Override
		public synchronized void execute(Runnable command) {
			(new Thread(command)).start();
		}
	};

	static {
		DateTimeFormat.setProvider(new DateTimeFormatProviderImpl());
	}

	private final ConnectorWrapper connectorWrapper = new ConnectorWrapper();
	private Executor executor;
	private TimerTask loginTimeoutTask;
	private Timer timer = null;

	// private FileTransferManager fileTransferManager;

	public Jaxmpp() {
		super();
		this.eventBus = new ThreadSafeEventBus();
		this.sessionObject = new J2SESessionObject();
		init();
	}

	public Jaxmpp(SessionObject sessionObject) {
		super();
		this.eventBus = new ThreadSafeEventBus();
		this.sessionObject = sessionObject;
		init();
	}

	protected void checkTimeouts() throws JaxmppException {
		ResponseManager.getResponseManager(sessionObject).checkTimeouts();
	}

	protected Connector createConnector() throws JaxmppException {
		if (sessionObject.getProperty(CONNECTOR_TYPE) == null || "socket".equals(sessionObject.getProperty(CONNECTOR_TYPE))) {
			log.info("Using SocketConnector");
			return new SocketConnector(context);
		} else if ("bosh".equals(sessionObject.getProperty(CONNECTOR_TYPE))) {
			log.info("Using BOSHConnector");
			return new BoshConnector(context);
		} else if ("websocket".equals(sessionObject.getProperty(CONNECTOR_TYPE))) {
			log.info("Using WebSocketConnector");
			return new WebSocketConnector(context);
		} else
			throw new JaxmppException("Unknown connector type");
	}

	@Override
	public void disconnect() throws JaxmppException {
		disconnect(false);
	}

	public void disconnect(boolean snc) throws JaxmppException {
		disconnect(snc, true);
	}

	public void disconnect(boolean snc, boolean resetStreamManagement) throws JaxmppException {
		try {
			if (this.connector != null) {
				Boolean sync = this.sessionObject.getProperty(SYNCHRONIZED_MODE);
				sync = snc || (sync != null && sync);
				Connector.DisconnectedHandler handler = null;
				if (sync) {
					handler = new Connector.DisconnectedHandler() {
						@Override
						public void onDisconnected(SessionObject sessionObject) {
							synchronized (Jaxmpp.this) {
								Jaxmpp.this.notifyAll();
							}
						}
					};
					this.eventBus.addHandler(Connector.DisconnectedHandler.DisconnectedEvent.class, handler);
				}
				try {
					this.connector.stop();
				} catch (XMLException e) {
					throw new JaxmppException(e);
				}
				if (sync) {
					synchronized (Jaxmpp.this) {
						if (getConnector().getState() != Connector.State.disconnected)
							Jaxmpp.this.wait();
					}
					this.eventBus.remove(Connector.DisconnectedHandler.DisconnectedEvent.class, handler);
				}
			}
		} catch (InterruptedException e) {
			throw new JaxmppException(e);
		} finally {
			if (resetStreamManagement) {
				StreamManagementModule.reset(sessionObject);
				sessionObject.clear(Scope.session);
			}
		}
	}

	@Override
	public void execute(Runnable runnable) {
		if (runnable != null)
			executor.execute(runnable);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ConnectionConfiguration getConnectionConfiguration() {
		return new ConnectionConfiguration(this.sessionObject);
	}

	public Executor getExecutor() {
		return executor;
	}

	/**
	 * Sets custom {@linkplain Executor} for processing incoming stanzas in
	 * modules.
	 *
	 * @param executor
	 *            executor
	 */
	public void setExecutor(Executor executor) {
		if (executor == null)
			this.executor = DEFAULT_EXECUTOR;
		else
			this.executor = executor;
	}

	@Override
	protected void init() {
		// if (PresenceModule.getPresenceStore(sessionObject) == null)
		// PresenceModule.setPresenceStore(sessionObject, new
		// J2SEPresenceStore());

		// if (RosterModule.getRosterStore(sessionObject) == null)
		// RosterModule.setRosterStore(sessionObject, new RosterStore());

		if (ResponseManager.getResponseManager(sessionObject) == null)
			ResponseManager.setResponseManager(sessionObject, new ThreadSafeResponseManager());

		super.init();

		setExecutor(DEFAULT_EXECUTOR);

		this.connector = this.connectorWrapper;

		this.processor = new Processor(this.modulesManager, context);

		modulesInit();
	}

	// public FileTransferManager getFileTransferManager() {
	// return fileTransferManager;
	// }

	@Override
	/**
	 * Connects to server in sync mode.
	 */
	public void login() throws JaxmppException {
		login(true);
	}

	// public void initFileTransferManager(boolean experimental) throws
	// JaxmppException {
	// CapabilitiesModule capsModule = getModule(CapabilitiesModule.class);
	// if (capsModule != null && capsModule.getCache() == null) {
	// capsModule.setCache(new J2SECapabiliesCache());
	// }
	//
	// fileTransferManager = new FileTransferManager();
	// fileTransferManager.setContext(getContext());
	// fileTransferManager.setJaxmpp(this);
	//
	// getModulesManager().register(new FileTransferModule(getContext()));
	// getModulesManager().register(new Socks5BytestreamsModule(getContext()));
	//
	// if (experimental) {
	// getModulesManager().register(new JingleModule(getContext()));
	// fileTransferManager.addNegotiator(new JingleFileTransferNegotiator());
	// }
	// fileTransferManager.addNegotiator(new Socks5FileTransferNegotiator());
	// }

	// public FileTransferManager getFileTransferManager() {
	// return fileTransferManager;
	// }

	// public void initFileTransferManager(boolean experimental) throws
	// JaxmppException {
	// CapabilitiesModule capsModule = getModule(CapabilitiesModule.class);
	// if (capsModule != null && capsModule.getCache() == null) {
	// capsModule.setCache(new J2SECapabiliesCache());
	// }
	//
	// fileTransferManager = new FileTransferManager();
	// fileTransferManager.setContext(context);
	// fileTransferManager.setJaxmpp(this);
	//
	// getModulesManager().register(new FileTransferModule(sessionObject));
	// getModulesManager().register(new Socks5BytestreamsModule(sessionObject));
	// if (experimental) {
	// getModulesManager().register(new JingleModule(sessionObject));
	// fileTransferManager.addNegotiator(new JingleFileTransferNegotiator());
	// }
	// fileTransferManager.addNegotiator(new Socks5FileTransferNegotiator());
	// }

	/**
	 * Connects to server.
	 *
	 * @param sync
	 *            <code>true</code> to start method in sync mode. In sync mode
	 *            whole connecting process will be done in this method.
	 */
	public synchronized void login(boolean sync) throws JaxmppException {
		synchronized (this) {
			if (timer != null)
				timer.cancel();

			timer = new Timer(true);
			timer.schedule(new CheckTimeoutsTask(), 30 * 1000, 30 * 1000);
		}
		this.modulesManager.initIfRequired();

		final Connector.State state = this.connectorWrapper.getState();
		if (state != Connector.State.disconnected) {
			log.info("Cannot login, because Connector.State is " + state);
			throw new JaxmppException("Connector is not in disconnected state");
		}

		this.sessionObject.clear(Scope.stream);

		if (this.sessionLogic != null) {
			this.sessionLogic.unbind();
			this.sessionLogic = null;
		}

		synchronized (this.connectorWrapper) {
			if (this.connectorWrapper.getConnector() != null) {
				log.log(Level.FINEST, "Found previous instance of Connector = {0}", connectorWrapper.getConnector());
				// There is no point in stopping old connector as it is not possible
				// to reach this point if connector is not in disconnected state, but
				// calling this code may in some cases lead to errors in further
				// use of this Jaxmpp instance.
//				try {
//					this.connectorWrapper.stop(true);
//					Thread.sleep(1000);
//				} catch (Exception e) {
//					log.log(Level.WARNING, "Something goes wrong during killing previous connector!", e);
//				}
//				this.connectorWrapper.setConnector(null);
			}
			this.connectorWrapper.setConnector(createConnector());
		}

		this.sessionLogic = connector.createSessionLogic(modulesManager, this.writer);
		this.sessionLogic.setSessionListener(new SessionListener() {

			@Override
			public void onException(JaxmppException e) throws JaxmppException {
				Jaxmpp.this.onException(e);
			}
		});

		try {
			this.sessionLogic.beforeStart();
			this.connector.start();
			this.sessionObject.setProperty(SYNCHRONIZED_MODE, Boolean.valueOf(sync));
			if (sync) {
				loginTimeoutTask = new LoginTimeoutTask();
				Long delay = sessionObject.getProperty(LOGIN_TIMEOUT_KEY);
				log.finest("Starting LoginTimeoutTask");
				timer.schedule(loginTimeoutTask, delay == null ? 1000 * 60 * 5 : delay);
				synchronized (Jaxmpp.this) {
					Jaxmpp.this.wait();
					log.finest("Waked up");
					Jaxmpp.this.wait(512);
				}

				if (loginTimeoutTask != null) {
					log.finest("Canceling LoginTimeoutTask");
					loginTimeoutTask.cancel();
					loginTimeoutTask = null;
				}
			}
			if (sessionObject.getProperty(EXCEPTION_KEY) != null) {
				JaxmppException r = sessionObject.getProperty(EXCEPTION_KEY);
				sessionObject.setProperty(EXCEPTION_KEY, null);
				JaxmppException e = new JaxmppException(r.getMessage(), r.getCause());
				throw r;
			}
		} catch (JaxmppException e) {
			// onException(e);
			throw e;
		} catch (Exception e1) {
			JaxmppException e = new JaxmppException(e1);
			// onException(e);
			throw e;
		}
	}

	@Override
	protected void modulesInit() {
		super.modulesInit();

		SaslModule saslModule = this.modulesManager.getModule(SaslModule.class);
		saslModule.addMechanism(new ExternalMechanism(), true);
	}

	@Override
	protected void onConnectorStopped() {
		super.onConnectorStopped();
		synchronized (this) {
			if (timer != null)
				timer.cancel();
			timer = null;
		}
	}

	@Override
	protected void onException(JaxmppException e) throws JaxmppException {
		log.log(Level.FINE, "Catching exception", e);
		sessionObject.setProperty(EXCEPTION_KEY, e);
		try {
			connector.stop();
		} catch (Exception e1) {
			log.log(Level.FINE, "Disconnecting error", e1);
		}
		synchronized (Jaxmpp.this) {
//			Jaxmpp.this.notify();
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
		}
		// XXX eventBus.fire(new LoggedOutEvent(sessionObject));
	}

	@Override
	protected void onResourceBindSuccess(JID bindedJID) throws JaxmppException {
		synchronized (Jaxmpp.this) {
			Jaxmpp.this.notify();
		}
		eventBus.fire(new LoggedInEvent(sessionObject));
	}

	@Override
	protected void onStreamError(StreamError condition, Throwable caught) throws JaxmppException {
		if (sessionObject.getProperty(Connector.RECONNECTING_KEY) != Boolean.TRUE) {
			synchronized (Jaxmpp.this) {
				Jaxmpp.this.notify();
			}
		}

		// XXX eventBus.fire(new LoggedOutEvent(sessionObject));
	}

	@Override
	protected void onStreamResumed(Long h, String previd) throws JaxmppException {
		synchronized (Jaxmpp.this) {
			Jaxmpp.this.notify();
		}
		eventBus.fire(new LoggedInHandler.LoggedInEvent(sessionObject));
	}

	@Override
	protected void onStreamTerminated() throws JaxmppException {
		if (sessionObject.getProperty(Connector.RECONNECTING_KEY) == null
				|| !(Boolean) sessionObject.getProperty(Connector.RECONNECTING_KEY)) {
			synchronized (Jaxmpp.this) {
				Jaxmpp.this.notify();
			}
		}
		// XXX eventBus.fire(new LoggedOutEvent(sessionObject));
	}

	private class CheckTimeoutsTask extends TimerTask {

		@Override
		public void run() {
			try {
				checkTimeouts();
			} catch (JaxmppException e) {
				log.warning("Problem on checking timeouts");
			}
		}

	}

	private class LoginTimeoutTask extends TimerTask {

		@Override
		public void run() {
			synchronized (Jaxmpp.this) {
				Jaxmpp.this.notify();
			}
		}
	}

}
