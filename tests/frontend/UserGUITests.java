package frontend;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import urChatBasic.base.Constants;
import utils.TestDriverGUI;
import static org.junit.Assert.assertNotEquals;
import static org.testng.AssertJUnit.*;

public class UserGUITests {
    Preferences baseTestPreference;
    Preferences serverTestPreference;
    Preferences channelTestPreference;

    @BeforeClass
    public void setUp() throws Exception {
        baseTestPreference = Constants.BASE_PREFS.parent().node("testing");
        serverTestPreference = baseTestPreference.node("servername");
        channelTestPreference = serverTestPreference.node("#channel");
    }

    @AfterClass
    public void tearDown() throws BackingStoreException {
        baseTestPreference.removeNode();
        TestDriverGUI.closeWindow();
    }

    @Test
    public void nodeExists() throws BackingStoreException {
        assertTrue(serverTestPreference.nodeExists("#channel"));
    }

    @Test
    public void nodeIsEmpty() throws BackingStoreException {
        assertEquals(0, channelTestPreference.keys().length + channelTestPreference.childrenNames().length);
    }

    @Test
    public void nodeNotEmpty() throws BackingStoreException {
        channelTestPreference.put("key","value");
        assertNotEquals(0, channelTestPreference.keys().length + channelTestPreference.childrenNames().length);
    }

}