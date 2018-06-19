/*
 * Tigase XMPP Client Library
 * Copyright (C) 2014 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.android.roster;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;

public class AndroidRosterStore extends RosterStore {

	private final RosterProvider provider;

	public AndroidRosterStore(RosterProvider provider) {
		this.provider = provider;
	}

	@Override
	protected Set<String> addItem(RosterItem item) {
		return this.provider.addItem(sessionObject, item);
	}

	@Override
	protected Set<String> calculateModifiedGroups(HashSet<String> groupsOld) {
		return groupsOld;
	}

	@Override
	public RosterItem get(BareJID jid) {
		return this.provider.getItem(sessionObject, jid);
	}

	@Override
	public List<RosterItem> getAll(Predicate predicate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCount() {
		return this.provider.getCount(sessionObject);
	}

	@Override
	public Collection<? extends String> getGroups() {
		return this.provider.getGroups(sessionObject);
	}

	@Override
	public void removeAll() {
		this.provider.removeAll(sessionObject);
	}

	@Override
	protected void removeItem(BareJID jid) {
		RosterItem item = this.provider.getItem(sessionObject, jid);
		this.provider.removeItem(sessionObject, item);
	}

}
