package backend;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import urChatBasic.base.Constants;

import static org.junit.Assert.*;

public class UserGUITests {
    Preferences baseTestPreference;
    Preferences serverTestPreference;
    Preferences roomTestPreference;

    @BeforeTest
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

    @AfterTest
    public void tearDown() throws BackingStoreException {
        baseTestPreference.removeNode();
    }
}