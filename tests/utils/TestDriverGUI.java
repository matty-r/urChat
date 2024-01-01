package utils;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import org.testng.Reporter;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.UserGUI;

public class TestDriverGUI extends DriverGUI
{
    final String testProfileName = "testingprofile" + (new SimpleDateFormat("yyMMddss")).format(new Date());
    final UserGUI testGUI;

    public String getTestProfileName ()
    {
        return testProfileName;
    }

    public TestDriverGUI () throws IOException
    {
        super();
        Reporter.log("Creating test gui", true);
        // TODO: We should just create a TestDriverGUI instead.
        DriverGUI.initLAFLoader();
        URProfilesUtil.createProfile(testProfileName);
        DriverGUI.createGUI(Optional.of(testProfileName));
        testGUI = DriverGUI.gui;
        testGUI.setupUserGUI();
        UserGUI.setTimeLineString(Constants.DEFAULT_TIME_STAMP_FORMAT);
        UserGUI.setNickFormatString(Constants.DEFAULT_NICK_FORMAT);
    }


}
