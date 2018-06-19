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
package tigase.jaxmpp.core.client.xmpp.modules.auth.saslmechanisms;

import tigase.jaxmpp.core.client.SessionObject;

public class AnonymousMechanism extends AbstractSaslMechanism {

	public AnonymousMechanism() {
	}

	@Override
	public String evaluateChallenge(String input, SessionObject sessionObjec) {
		setComplete(sessionObjec, true);
		return null;
	}

	@Override
	public boolean isAllowedToUse(SessionObject sessionObject) {
		return true;
	}

	@Override
	public String name() {
		return "ANONYMOUS";
	}

}