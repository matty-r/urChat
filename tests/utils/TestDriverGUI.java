package utils;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.testng.Reporter;
import static org.testng.Reporter.log;
import static urChatBasic.backend.utils.LogPatternParser.parseLogLineFull;
import urChatBasic.backend.utils.ReverseLineInputStream;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import urChatBasic.base.IRCChannelBase;
import urChatBasic.base.IRCServerBase;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.IRCServer;
import urChatBasic.frontend.UserGUI;
import urChatBasic.frontend.panels.InterfacePanel;

public class TestDriverGUI extends DriverGUI
{
    final String testProfileName = "testingprofile" + (new SimpleDateFormat("yyMMddss")).format(new Date());
    static List<String> testProfiles = new ArrayList<>();
    static boolean loadingLogsInProgress = false;

    public String getTestProfileName ()
    {
        return testProfileName;
    }

    public TestDriverGUI () throws IOException, InvocationTargetException, InterruptedException
    {
        log("Creating test gui", true);
        Constants.init();
        initLAFLoader();

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                frame = new JFrame("urChat");
                log("Creating test profile [" + testProfileName + "]", true);
                URProfilesUtil.createProfile(testProfileName);
                testProfiles.add(testProfileName);
                // This will load the default profile
                log("Initialize test gui using test profile", true);
                gui = createGUI(Optional.of(testProfileName));
                gui.setTimeLineString(Constants.DEFAULT_TIME_STAMP_FORMAT);
                gui.setNickFormatString(Constants.DEFAULT_NICK_FORMAT);
                // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(gui);
                frame.pack();
                frame.setVisible(false);
            }
        });
    }

    public static void waitForEverything (UserGUI gui) throws InterruptedException
    {
        boolean wait = true;
        // wait for message queue to finish, and for updating styles to finish
        while (wait)
        {
            wait = false;
            TimeUnit.SECONDS.sleep(1);

            if (gui.previewLineFormatter != null && gui.previewLineFormatter.updateStylesInProgress.get())
            {
                log("Update styles in Progress.. waiting", true);
                wait = true;
                continue;
            }

            if(loadingLogsInProgress)
            {
                log("Loading logs in Progress.. waiting", true);
                wait = true;
                continue;
            }

            for (IRCServerBase server : gui.getCreatedServers())
            {
                for (IRCChannelBase channel : ((IRCServer) server).createdChannels)
                {
                    if (channel.messageQueueInProgress)
                    {
                        log("Message Queue in Progress.. waiting", true);
                        wait = true;
                        break;
                    }
                    if (channel.getLineFormatter().updateStylesInProgress.get())
                    {
                        log("Update styles in Progress.. waiting", true);
                        wait = true;
                        break;
                    }
                }
            }
        }
    }

    public static void cleanupTestProfiles ()
    {
        URProfilesUtil.setActiveProfileName(URProfilesUtil.getDefaultProfile());
        for (String testProfileName : testProfiles) {
            Reporter.log("Deleting testing profile ["+testProfileName+"]", true);
            URProfilesUtil.deleteProfile(testProfileName, false);
        }
    }

    public static void startTestGUI (UserGUI gui) throws InterruptedException
    {
        // waitForEverything(gui);
        SwingUtilities.invokeLater(gui);
        log("Started", true);
    }

    public static void closeWindow ()
    {
        WindowEvent closingEvent = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closingEvent);
        // frame.dispose();
    }

    public void loadTestLogs (IRCChannelBase channel) throws InterruptedException
    {
        Thread fileReadingThread = new Thread(() -> {
            loadingLogsInProgress = true;
            File logFile = new File("tests/Logs/testlogs-#somechannel.log");
            String line = "";
            try (BufferedReader br = new BufferedReader (new InputStreamReader (new ReverseLineInputStream(logFile)))) {
                int maxCount = ((InterfacePanel) gui.interfacePanel).getLimitChannelLinesCount() * 2;
                log("Load max count: [" + maxCount +"] Loading logs into " + channel.getName(), true);
                int loadCount = 0;
                while ((line = br.readLine()) != null && loadCount < maxCount) {
                    loadingLogsInProgress = true;
                    // Only wait if the queue is full
                    while(channel.messageQueueFull())
                    {
                        System.out.println("Sleeping log loading thread.");
                        Thread.sleep(10);
                    }

                    Map<String, Object> parsedLine = parseLogLineFull(line);
                    if(parsedLine.size() != 0)
                    {
                        channel.printText((Date) parsedLine.get("DATE"), parsedLine.get("MESSAGE").toString(), parsedLine.get("USER").toString());
                        loadCount ++;
                    }
                }
                log("Done loading logs " + channel.getName(), true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                loadingLogsInProgress = false;
            }
        });
        fileReadingThread.start();
    }
}
