package tigase.jaxmpp.j2se;

import junit.framework.Assert;

import org.junit.Test;

public class JaxmppTest {

	@Test
	public void test() {
		try {
			Jaxmpp j = new Jaxmpp();
			j.getConnectionConfiguration().setServer("WIN-9SGIS87O21N");
			j.getConnectionConfiguration().setUserJID("admin@win-9sgis87o21n");
			j.getConnectionConfiguration().setUserPassword("tigase");
			j.login(true);
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}

	}

}
