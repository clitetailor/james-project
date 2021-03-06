/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 * http://www.apache.org/licenses/LICENSE-2.0                   *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.apache.commons.net.imap.IMAPClient;
import org.apache.james.user.ldap.LdapGenericContainer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class CassandraLdapJamesServerTest extends AbstractJmapJamesServerTest {
    private static final String JAMES_USER = "james-user";
    private static final String PASSWORD = "secret";
    private static final String DOMAIN = "james.org";
    private static final String ADMIN_PASSWORD = "mysecretpassword";

    private LdapGenericContainer ldapContainer = LdapGenericContainer.builder()
        .domain(DOMAIN)
        .password(ADMIN_PASSWORD)
        .build();
    private CassandraLdapJmapTestRule cassandraLdapJmap = CassandraLdapJmapTestRule.defaultTestRule();
    private IMAPClient imapClient = new IMAPClient();

    @Rule
    public RuleChain ruleChain = RuleChain.outerRule(ldapContainer).around(cassandraLdapJmap);

    @Override
    protected GuiceJamesServer createJamesServer() throws IOException {
        ldapContainer.start();
        return cassandraLdapJmap.jmapServer(ldapContainer.getLdapHost());
    }

    @Override
    protected void clean() {
        if (ldapContainer != null) {
            ldapContainer.stop();
        }

        try {
            imapClient.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void userFromLdapShouldLoginViaImapProtocol() throws Exception {
        imapClient.connect(JAMES_SERVER_HOST, IMAP_PORT);

        assertThat(imapClient.login(JAMES_USER, PASSWORD)).isTrue();
    }
}
