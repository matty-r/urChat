package utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.testng.annotations.Test;

public class LogPatternParser
{

    // Define enum for log patterns
    public enum LogPattern
    {
        DATE("%d", "(?<DATE>.*UTC)", Date.class),
        SERVER("%marker", "(?<SERVER>^[A-Za-z0-9.-]+)-", String.class),
        CHANNEL("%marker", "(?<CHANNEL>^#.*?)\\s", String.class), // Named group for channel, excluding the trailing whitespace
        USER("%msg|%message", "(?<USER>^.*?):", String.class),
        MESSAGE("%msg|%message", "(?<MESSAGE>.*)", String.class);

        private final String pattern;
        private final String regex;
        private final Class<?> patternClass;

        LogPattern (String pattern, String regex, Class<?> patternClass)
        {
            this.pattern = pattern;
            this.regex = regex;
            this.patternClass = patternClass;
        }

        public String getPattern ()
        {
            return pattern;
        }

        public Class<?> getPatternClass ()
        {
            return patternClass;
        }

        public String getRegex ()
        {
            return regex;
        }

        public String getMatchGroup ()
        {
            return this.toString();
        }

        public String getPatternLayout ()
        {
            return "%d{yyy-MM-dd HH:mm:ss.SSS}{UTC}UTC %marker %msg%n";
        }
    }

    public static Date parseDate (String dateString) {

        // Step 1: Define the DateTimeFormatter with the pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        // Step 2: Parse the string into a TemporalAccessor object using the formatter
        TemporalAccessor temporalAccessor = formatter.parse(dateString.replace("UTC", ""));

        // Step 3: Convert the TemporalAccessor to a LocalDateTime object
        LocalDateTime localDateTime = LocalDateTime.from(temporalAccessor);

        // Step 4: Convert the LocalDateTime to the local timezone
        LocalDateTime localDateTimeInLocalTimeZone = localDateTime
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();

        // Step 5: Convert the LocalDateTime to a Date object
        Date date = Date.from(localDateTimeInLocalTimeZone.atZone(ZoneId.systemDefault()).toInstant());


        return date;
    }

    // Parse log line using specified pattern
    public static void parseLogLine (String logLine)
    {
        Map<String, Object> parsedValues = new HashMap<String, Object>();

        for (LogPattern pattern : LogPattern.values())
        {
            Pattern regexPattern = Pattern.compile(pattern.getRegex());
            Matcher matcher = regexPattern.matcher(logLine);
            if (matcher.find())
            {
                String fullMatch = matcher.group(0);
                String match = matcher.group(pattern.getMatchGroup());
                System.out.println(pattern.name() + " group: " + match);
                // parsedValues.put(pattern.toString(), match);
                switch (pattern.getPatternClass().getSimpleName()) {
                    case "Date":
                            parsedValues.put(pattern.toString(), parseDate(match));
                            logLine = logLine.replaceFirst(fullMatch, "").trim();
                        break;
                    default:
                            parsedValues.put(pattern.toString(), match);
                            if(logLine.length() == fullMatch.length())
                                break;

                            logLine = logLine.replaceFirst(fullMatch, "").trim();
                        break;
                }
            }
        }

        System.out.println("Done");
    }

    public static void parseLogLineFull (String logLine) {
        Map<String, Object> parsedValues = new HashMap<>();

        StringBuilder combinedRegexBuilder = new StringBuilder();
        for (LogPattern pattern : LogPattern.values()) {
            if (combinedRegexBuilder.length() > 0) {
                combinedRegexBuilder.append("|");
            }
            combinedRegexBuilder.append("(").append(pattern.getRegex()).append(")");
        }
        String combinedRegex = combinedRegexBuilder.toString();

        Pattern regexPattern = Pattern.compile(combinedRegex);
        Matcher matcher = regexPattern.matcher(logLine);
        while (matcher.find()) {
            for (LogPattern pattern : LogPattern.values()) {
                if (matcher.group(pattern.getPattern()) != null) {
                    String fullMatch = matcher.group(0);
                    String match = matcher.group(pattern.getMatchGroup());
                    System.out.println(pattern.name() + " group: " + match);
                    switch (pattern.getPatternClass().getSimpleName()) {
                        case "Date":
                            parsedValues.put(pattern.toString(), parseDate(match));
                            break;
                        default:
                            parsedValues.put(pattern.toString(), match);
                            break;
                    }
                    logLine = logLine.replaceFirst(fullMatch, "").trim();
                    break; // Break to the outer loop to handle the next log line part
                }
            }
        }

        System.out.println("Done");
    }

    @Test
    public void testParsing ()
    {
        String logLine = "2024-02-13 19:27:31.414UTC irc.libera.chat-#java matty_r: morning:asdjnwk 123AD?asd,123uADAjkalas[];'das[]";
        // parseLogLine(logLine);
        parseLogLineFull(logLine);
    }
}
