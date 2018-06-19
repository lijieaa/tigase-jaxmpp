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

/**
 * XMPP entity address form <code>&lt;localpart@domainpart&gt;</code>
 */
public class BareJID implements Comparable<BareJID> {

	/**
	 * Creates instance of {@link BareJID}.
	 * 
	 * @param jid
	 *            string contains full JID or bare JID
	 * @return bare JID
	 */
	public static BareJID bareJIDInstance(String jid) {
		String[] parsedJid = parseJID(jid);
		return bareJIDInstance(parsedJid[0], parsedJid[1]);
	}

	/**
	 * Creates instance of {@link BareJID}.
	 * 
	 * @param p_localpart
	 *            localpart
	 * @param p_domain
	 *            domainpart
	 * @return bare JID
	 */
	public static BareJID bareJIDInstance(String p_localpart, String p_domain) {
		return new BareJID(p_localpart, p_domain);
	}

	static String[] parseJID(String jid) {
		String[] result = new String[3];

		// Cut off the resource part first
		int idx = jid.indexOf('/');

		// Resource part:
		result[2] = ((idx == -1) ? null : jid.substring(idx + 1));

		String id = ((idx == -1) ? jid : jid.substring(0, idx));

		// Parse the localpart and the domain name
		idx = id.indexOf('@');
		result[0] = ((idx == -1) ? null : id.substring(0, idx));
		result[1] = ((idx == -1) ? id : id.substring(idx + 1));

		return result;
	}

	private static String toString(String p_localpart, String p_domain) {
		return (((p_localpart != null) && (p_localpart.length() > 0)) ? (p_localpart + "@" + p_domain) : p_domain);
	}

	private final String $toString;

	protected final String domain;

	protected final String localpart;

	protected BareJID(String localpart, String domain) {
		this.localpart = localpart != null ? localpart.intern() : null;
		this.domain = domain.toLowerCase().intern();
		this.$toString = toString(this.localpart, this.domain);
	}

	@Override
	public int compareTo(BareJID o) {
		return $toString.compareTo(o.$toString);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BareJID))
			return false;
		BareJID other = (BareJID) obj;
		if ($toString == null) {
			if (other.$toString != null)
				return false;
		} else if (!$toString.equals(other.$toString))
			return false;
		return true;
	}

	/**
	 * Return domainpart.
	 * 
	 * @return domainpart
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * Return localpart.
	 * 
	 * @return localpart
	 */
	public String getLocalpart() {
		return localpart;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (($toString == null) ? 0 : $toString.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return $toString;
	}

}