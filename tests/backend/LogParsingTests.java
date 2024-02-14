package backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.Test;
import urChatBasic.backend.utils.LogPatternParser;
import static org.testng.AssertJUnit.assertEquals;
import static urChatBasic.backend.utils.LogPatternParser.LogPattern;

public class LogParsingTests {


    @Test
    public void testLogLinesManual ()
    {
        testParsing("2024-02-13 19:27:31.414UTC irc.libera.chat-#java matty_r: morning:asdjnwk 123AD?asd,123uADAjkalas[];'das[]");
        testParsing("2024-01-31 20:49:43.003UTC irc.libera.chat-#linux ****: You have joined #linux");
        testParsing("2024-02-11 02:17:40.207UTC irc.libera.chat-#linux ****: Welcome to #linux! Help & support for any Linux distribution or related topic -- Rules/Info: https://linux.chat -- Forum: https://linux.forum -- Pastebin: https://paste.linux.chat/ -- @linux.social on Mastodon: https://linux.social -- Need an op? !ops <reason> or join #linux-ops");
        testParsing("2024-01-31 20:58:55.016UTC irc.libera.chat-#linux user: 😢😢😢😢😢😢😢😢😢😢😢");
    }

    @Test
    public void testLogLines() {
        String logFilePath = "/home/matt/urChat/Logs/irc.libera.chat-#linux.log";
        String line = "";

        try (BufferedReader br = new BufferedReader(new FileReader(logFilePath))) {
            while ((line = br.readLine()) != null) {
                testParsing(line);
            }
        } catch (Exception e) {
            System.out.println(line);
            e.printStackTrace();
        }
    }

    public void testParsing (String logLine)
    {
        Map<String, Object> parsedValues = new HashMap<>();

        // parseLogLine(logLine);
        // parsedValues = parseLogLineFull(logLine);
        parsedValues = LogPatternParser.parseLogLineFull(logLine);

        String assertString = "";

        for (LogPattern logPattern : LogPattern.values()) {
            Object parsedValue = parsedValues.get(logPattern.toString());

            switch (logPattern.getPatternClass().getSimpleName()) {
                case "Date":
                    assertString += LogPatternParser.formatDateToString((Date) parsedValue);
                    break;
                default:
                    assertString += parsedValue.toString();
                    break;
            }

            assertString += logPattern.getAppendString();
        }

        assertEquals(logLine.trim(), assertString.trim());
    }
}
