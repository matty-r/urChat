package frontend;

import static org.testng.AssertJUnit.*;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import javax.swing.UIManager;
import javax.swing.text.StyleConstants;
import org.testng.Reporter;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import urChatBasic.frontend.UserGUI;
import urChatBasic.frontend.utils.URColour;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.backend.utils.URStyle;
import urChatBasic.base.Constants;
import urChatBasic.frontend.DriverGUI;

public class LAFTests
{
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
        Reporter.log("Deleting testing profile.", true);
        if(URProfilesUtil.getActiveProfileName().equals(testProfileName))
            URProfilesUtil.deleteProfile();
    }

    @Test(description = "Check that changing the Look and Feel, also correctly changes the style of the text")
    public void changingLAFChangesStyle() throws Exception
    {
        // Get current LAF name
        String currentLAF = UIManager.getLookAndFeel().getClass().getName();

        if(!currentLAF.equals("com.sun.java.swing.plaf.motif.MotifLookAndFeel"))
            testGUI.setNewLAF("com.sun.java.swing.plaf.motif.MotifLookAndFeel");

        URStyle newStyle = testGUI.getStyle();

        assertEquals("New Style should have the background colour of "+Constants.DEFAULT_BACKGROUND_STRING, URColour.hexEncode(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING)), URColour.hexEncode(newStyle.getBackground().get()));
    }

    @Test(description = "Changing LAF, updates the colours in the preview text area.")
    public void changingLAFUpdatesPreviewStyle() throws Exception
    {
        // Get current LAF name
        String currentLAF = UIManager.getLookAndFeel().getClass().getName();

        if(!currentLAF.equals("com.sun.java.swing.plaf.motif.MotifLookAndFeel"))
            testGUI.setNewLAF("com.sun.java.swing.plaf.motif.MotifLookAndFeel");

        // URStyle newStyle = testGUI.getStyle();

        Color newBackgroundColor = (Color) UserGUI.previewLineFormatter.getStyleAtPosition(0, "urChat has loaded - this is an Event").getAttribute(StyleConstants.Background);
        Color lineFormatterBackground = UserGUI.previewTextArea.getBackground();

        assertEquals("previewTextArea background colour should be the default", URColour.hexEncode(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING)), URColour.hexEncode(lineFormatterBackground));
        assertEquals("Line should have the background colour of "+Constants.DEFAULT_BACKGROUND_STRING, URColour.hexEncode(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING)), URColour.hexEncode(newBackgroundColor));
    }

    @Test(description = "Changing LAF and saving, correctly loads the new style")
    // TODO
    public void saveAndLoadLAFStyle() throws Exception
    {
    //     // Get current LAF name
    //     String currentLAF = UIManager.getLookAndFeel().getClass().getName();

    //     if(!currentLAF.equals("com.sun.java.swing.plaf.motif.MotifLookAndFeel"))
    //         testGUI.setNewLAF("com.sun.java.swing.plaf.motif.MotifLookAndFeel");

    //     // URStyle newStyle = testGUI.getStyle();

    //     Color newBackgroundColor = (Color) UserGUI.previewLineFormatter.getStyleAtPosition(0, "urChat has loaded - this is an Event").getAttribute(StyleConstants.Background);
    //     Color lineFormatterBackground = UserGUI.previewTextArea.getBackground();

    //     assertEquals("previewTextArea background colour should be the default", URColour.hexEncode(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING)), URColour.hexEncode(lineFormatterBackground));
    //     assertEquals("Line should have the background colour of "+Constants.DEFAULT_BACKGROUND_STRING, URColour.hexEncode(UIManager.getColor(Constants.DEFAULT_BACKGROUND_STRING)), URColour.hexEncode(newBackgroundColor));
    //
    }
}
