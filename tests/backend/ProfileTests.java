package backend;

import static org.testng.AssertJUnit.*;
import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import urChatBasic.backend.utils.URPreferencesUtil;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import utils.TestDriverGUI;

public class ProfileTests
{
    TestDriverGUI testDriver;
    // final String testLAFName

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception
    {
        testDriver = new TestDriverGUI();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown () throws Exception
    {
        Reporter.log("Deleting testing profile.", true);
        URProfilesUtil.deleteProfile(testDriver.getTestProfileName());
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
    public void deleteTestProfileTest ()
    {
        URProfilesUtil.createProfile(testDriver.getTestProfileName());
        URProfilesUtil.setActiveProfileName(testDriver.getTestProfileName());
        assertTrue(URProfilesUtil.getActiveProfileName().equals(testDriver.getTestProfileName()));
        // Delete the active profile
        URProfilesUtil.deleteProfile(testDriver.getTestProfileName());
        assertFalse(URProfilesUtil.profileExists(testDriver.getTestProfileName()));
    }

    @Test
    public void createProfileAndDeleteTest ()
    {
        String anotherTestProfileName = "createProfileAndDeleteTest" + (new SimpleDateFormat("yyMMdd")).format(new Date());
        URProfilesUtil.createProfile(anotherTestProfileName);
        // Profile Exists
        assertTrue(URProfilesUtil.profileExists(anotherTestProfileName));

        URProfilesUtil.setActiveProfileName(anotherTestProfileName);

        // Has the default setting
        assertEquals(Constants.DEFAULT_TIME_STAMP_FORMAT, URProfilesUtil.getActiveProfilePath().get(Constants.KEY_TIME_STAMP_FORMAT, "ERROR!"));

        URProfilesUtil.deleteProfile(anotherTestProfileName);
    }

    @Test
    public void invalidProfileTest ()
    {
        String originalActiveProfile = URProfilesUtil.getActiveProfileName();
        String anotherTestProfileName = "invalidProfileTest" + (new SimpleDateFormat("yyMMdd")).format(new Date());
        // Profile Exists
        assertFalse("Profile ["+anotherTestProfileName+"] shouldn't exist!",URProfilesUtil.profileExists(anotherTestProfileName));

        URProfilesUtil.setActiveProfileName(anotherTestProfileName);

        assertEquals(originalActiveProfile, URProfilesUtil.getActiveProfileName());
    }

    @Test
    public void cloneProfileTest () throws BackingStoreException
    {
        Preferences originalPathRoot = URProfilesUtil.getProfilePath(testDriver.getTestProfileName());
        Preferences clonedProfileRoot = URProfilesUtil.cloneProfile(testDriver.getTestProfileName(), Optional.empty());

        ArrayList<Preferences> originalNodes = URPreferencesUtil.getAllNodes(originalPathRoot);

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Delete the cloned profile
        clonedProfileRoot.removeNode();
    }

    @Test
    public void switchToClonedProfileTest () throws BackingStoreException
    {
        Preferences clonedProfileRoot = URProfilesUtil.cloneProfile(testDriver.getTestProfileName(), Optional.empty());
        final String clonedProfileName;

        clonedProfileName = Arrays.stream(URProfilesUtil.getProfiles()).filter(e -> clonedProfileRoot.toString().endsWith(e)).findFirst().get();

        URProfilesUtil.setActiveProfileName(clonedProfileName);
        // Delete the cloned profile
        clonedProfileRoot.removeNode();
    }
}
