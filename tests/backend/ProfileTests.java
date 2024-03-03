package backend;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.Reporter.log;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import urChatBasic.backend.utils.URPreferencesUtil;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import utils.TestDriverGUI;

public class ProfileTests
{
    TestDriverGUI testDriver;
    // final String testLAFName

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception
    {
        testDriver = new TestDriverGUI();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown () throws Exception
    {
        TestDriverGUI.cleanupTestProfiles();
        TestDriverGUI.closeWindow();
    }

    // 1. Test creating a profile
    // 2. Test deleting a profile
    // 3. Test setting profile as default, then creating a new DriverGUI to see if it loads that profile
    // 4. Test loading a default profile that doesn't exist

    @Test
    public void createdTestProfileTest ()
    {
        assertTrue(URProfilesUtil.profileExists(testDriver.getTestProfileName()));
    }

    @Test
    public void deleteTestProfileTest () throws InterruptedException
    {
        log("Check it exists", true);
        if(!URProfilesUtil.profileExists(testDriver.getTestProfileName()))
            URProfilesUtil.createProfile(testDriver.getTestProfileName());

        TestDriverGUI.waitForEverything(TestDriverGUI.gui);
        URProfilesUtil.setActiveProfileName(testDriver.getTestProfileName());
        TestDriverGUI.waitForEverything(TestDriverGUI.gui);
        assertTrue(URProfilesUtil.getActiveProfileName().equals(testDriver.getTestProfileName()));
        // Delete the active profile
        URProfilesUtil.deleteProfile(testDriver.getTestProfileName(), false);
        assertFalse(URProfilesUtil.profileExists(testDriver.getTestProfileName()));
    }

    @Test
    public void createProfileAndDeleteTest () throws InterruptedException
    {
        String anotherTestProfileName = "createProfileAndDeleteTest" + (new SimpleDateFormat("yyMMdd")).format(new Date());
        log("Create Profile ["+anotherTestProfileName+"]", true);
        anotherTestProfileName = URProfilesUtil.createProfile(anotherTestProfileName);

        log("Wait for stuff", true);
        TestDriverGUI.waitForEverything(TestDriverGUI.gui);
        // Profile Exists
        assertTrue(URProfilesUtil.profileExists(anotherTestProfileName));

        log("Set profile ["+anotherTestProfileName+"]", true);
        URProfilesUtil.setActiveProfileName(anotherTestProfileName);
        TestDriverGUI.waitForEverything(TestDriverGUI.gui);
        // Has the default setting
        assertEquals(Constants.DEFAULT_TIME_STAMP_FORMAT, URProfilesUtil.getActiveProfilePath().get(Constants.KEY_TIME_STAMP_FORMAT, "ERROR!"));

        URProfilesUtil.deleteProfile(anotherTestProfileName, false);
    }

    @Test
    public void invalidProfileTest () throws InterruptedException
    {
        log("Active Profile [" + URProfilesUtil.getActiveProfileName() + "]", true);
        String originalActiveProfile = URProfilesUtil.getActiveProfileName();
        String anotherTestProfileName = "invalidProfileTest" + (new SimpleDateFormat("yyMMdd")).format(new Date());
        // Profile Exists
        assertFalse("Profile ["+anotherTestProfileName+"] shouldn't exist!",URProfilesUtil.profileExists(anotherTestProfileName));
        TestDriverGUI.waitForEverything(TestDriverGUI.gui);
        URProfilesUtil.setActiveProfileName(anotherTestProfileName);
        TestDriverGUI.waitForEverything(TestDriverGUI.gui);
        assertEquals(originalActiveProfile, URProfilesUtil.getActiveProfileName());
    }

    @Test
    public void cloneProfileTest () throws BackingStoreException, InterruptedException
    {
        log("Loading Profile [" + testDriver.getTestProfileName() + "]", true);
        Preferences originalPathRoot = URProfilesUtil.getProfilePath(testDriver.getTestProfileName());

        log("Clone Profile [" + testDriver.getTestProfileName() + "]", true);
        Preferences clonedProfileRoot = URProfilesUtil.cloneProfile(testDriver.getTestProfileName(), Optional.empty());

        ArrayList<Preferences> originalNodes = URPreferencesUtil.getAllNodes(originalPathRoot);
        TestDriverGUI.waitForEverything(TestDriverGUI.gui);
        log("Checking preferences match original profile", true);
        for (Preferences originalPrefPath : originalNodes) {
            Preferences clonedPath = clonedProfileRoot;

            String[] childNodes = Path.of(originalPrefPath.absolutePath().replace(originalPathRoot.absolutePath(), "")).toString().split("/");

            for (String childName : childNodes) {
                clonedPath = clonedPath.node(childName);
            }

            try
            {
                for (String originalKey : originalPrefPath.keys()) {
                    assertEquals(URPreferencesUtil.getPref(originalKey, null, originalPrefPath), URPreferencesUtil.getPref(originalKey, null, clonedPath));
                }
            } catch (BackingStoreException e)
            {
                Constants.LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }

        // Delete the cloned profile
        URProfilesUtil.deleteProfile(clonedProfileRoot.name(), false);
    }

    @Test
    public void switchToClonedProfileTest () throws BackingStoreException, InterruptedException
    {
        Preferences clonedProfileRoot = URProfilesUtil.cloneProfile(testDriver.getTestProfileName(), Optional.empty());
        final String clonedProfileName;
        TestDriverGUI.waitForEverything(TestDriverGUI.gui);
        clonedProfileName = Arrays.stream(URProfilesUtil.getProfiles()).filter(e -> clonedProfileRoot.toString().endsWith(e)).findFirst().get();

        URProfilesUtil.setActiveProfileName(clonedProfileName);

        TestDriverGUI.waitForEverything(TestDriverGUI.gui);
        // Delete the cloned profile
        URProfilesUtil.deleteProfile(clonedProfileRoot.name(), false);
    }
}
