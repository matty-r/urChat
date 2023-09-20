package urChatBasic.tests.backend;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import urChatBasic.base.Constants;

import static org.junit.Assert.*;

public class UserGUITests {
    Preferences baseTestPreference;
    Preferences serverTestPreference;
    Preferences roomTestPreference;

    @Before
    public void setUp() throws Exception {
        baseTestPreference = Constants.BASE_PREFS.parent().node("testing");
        serverTestPreference = baseTestPreference.node("servername");
        roomTestPreference = serverTestPreference.node("#channel");
    }

    @Test
    public void nodeExists() throws BackingStoreException {
        assertTrue(serverTestPreference.nodeExists("#channel"));
    }

    @Test
    public void nodeIsEmpty() throws BackingStoreException {
        assertEquals(0, roomTestPreference.keys().length + roomTestPreference.childrenNames().length);
    }

    @Test
    public void nodeNotEmpty() throws BackingStoreException {
        roomTestPreference.put("key","value");
        assertNotEquals(0, roomTestPreference.keys().length + roomTestPreference.childrenNames().length);
    }

    @After
    public void tearDown() throws BackingStoreException {
        baseTestPreference.removeNode();
    }
}