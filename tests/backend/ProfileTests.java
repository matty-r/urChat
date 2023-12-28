package backend;

import static org.testng.AssertJUnit.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import org.testng.Reporter;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.UserGUI;

public class ProfileTests {

    DriverGUI testDriver;
    UserGUI testGUI;
    final String testProfileName = "testingprofile" + (new SimpleDateFormat("yyMMdd")).format(new Date());
    // final String testLAFName

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception
    {
        Reporter.log("Creating test gui", true);
        // TODO: We should just create a TestDriverGUI instead.
        testDriver = new DriverGUI();
        DriverGUI.initLAFLoader();
        DriverGUI.createGUI(Optional.of(testProfileName));
        testGUI = DriverGUI.gui;
        testGUI.setupUserGUI();
        Reporter.log("Setting profile to " + testProfileName, true);
        testGUI.setActiveProfile(testProfileName);
        testGUI.getClientSettings(true);
    }

    @AfterTest(alwaysRun = true)
    public void tearDown () throws Exception
    {
        if(URProfilesUtil.getActiveProfileName().equals(testProfileName))
        {
            Reporter.log("Deleting testing profile.", true);
            URProfilesUtil.deleteProfile();
        }
    }

    // 1. Test creating a profile
    // 2. Test deleting a profile
    // 3. Test setting profile as default, then creating a new DriverGUI to see if it loads that profile
    // 4. Test loading a default profile that doesn't exist

    @Test
    public void createdTestProfileTest ()
    {
        assertTrue(URProfilesUtil.profileExists(testProfileName));
    }

    @Test
    public void deleteTestProfileTest ()
    {
        assertTrue(URProfilesUtil.getActiveProfileName().equals(testProfileName));
        // Delete the active profile
        URProfilesUtil.deleteProfile();
        assertFalse(URProfilesUtil.profileExists(testProfileName));
    }

    @Test
    public void createProfileAndDeleteTest ()
    {
        String anotherTestProfileName = "anothertestingprofile" + (new SimpleDateFormat("yyMMdd")).format(new Date());
        URProfilesUtil.createProfile(anotherTestProfileName);
        // Profile Exists
        assertTrue(URProfilesUtil.profileExists(anotherTestProfileName));

        // Has the default setting
        assertEquals(Constants.DEFAULT_TIME_STAMP_FORMAT, URProfilesUtil.getProfilePath().get(Constants.KEY_TIME_STAMP_FORMAT, "ERROR!"));

        URProfilesUtil.deleteProfile(anotherTestProfileName);
    }

    @Test
    public void invalidProfileTest ()
    {
        String originalActiveProfile = URProfilesUtil.getActiveProfileName();
        String anotherTestProfileName = "anothertestingprofile" + (new SimpleDateFormat("yyMMdd")).format(new Date());
        // Profile Exists
        assertFalse("Profile ["+anotherTestProfileName+"] shouldn't exist!",URProfilesUtil.profileExists(anotherTestProfileName));

        URProfilesUtil.setActiveProfileName(anotherTestProfileName);

        assertEquals(originalActiveProfile, URProfilesUtil.getActiveProfileName());
    }

    // @Test
    // public void loadInvalidProfileTest ()
    // {
    //     DriverGUI testInvalidDriver = new DriverGUI();
    //     DriverGUI.createGUI(Optional.of(testProfileName));
    // }
}
