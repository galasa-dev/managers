/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.sdv.ISdvUser;
import dev.galasa.sdv.SdvManagerException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class TestRecordingRegion {

    RecordingRegion rr;
    ICredentialsUsernamePassword credentials;

    @BeforeEach
    void beforeEach() throws CredentialsException  {
        rr = new RecordingRegion(null);

        credentials =
                new CredentialsUsernamePassword(null, "user1", "password1");
    }

    @Test
    void testAddingNewUserToRegion() throws SdvManagerException, CredentialsException {
        // No users to start
        List<ISdvUser> beforeList = rr.getRecordingUsers();
        Assertions.assertEquals(0, beforeList.size());

        // Add new user
        ISdvUser newUser = new SdvUserImpl("creds1", credentials, "cics1", "TELLER");
        rr.addUserToRecord(newUser);

        // Number of users increment, and user is the one created
        List<ISdvUser> afterList = rr.getRecordingUsers();
        Assertions.assertEquals(1, afterList.size());
        Assertions.assertEquals(newUser, afterList.get(0));
    }

    @Test
    void testAddingExistingUserToRegion() throws SdvManagerException, CredentialsException {
        // Add existing user
        ISdvUser existingUser = new SdvUserImpl("creds1", credentials, "cics1", "TELLER");
        rr.addUserToRecord(existingUser);
        List<ISdvUser> beforeList = rr.getRecordingUsers();
        Assertions.assertEquals(1, beforeList.size());
        Assertions.assertEquals(existingUser, beforeList.get(0));

        // Create new user with same user name
        ISdvUser newUser = new SdvUserImpl("creds2", credentials, "cics1", "ADMIN");
        Throwable exception = Assertions.assertThrows(
            SdvManagerException.class,
            () -> rr.addUserToRecord(newUser)
        );

        Assertions.assertEquals(
            "User 'user1' has been allocated to more than one region in the test."
            + " Please report this to the Galasa project.",
            exception.getMessage()
        );
    }
}
