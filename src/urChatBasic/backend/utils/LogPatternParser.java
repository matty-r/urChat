package urChatBasic.backend.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogPatternParser
{

    // Define enum for log patterns
    public enum LogPattern
    {
        DATE("%d", "(?<DATE>.*UTC?)", Date.class, "UTC "),
        SERVER("%marker", "\\s(?<SERVER>[A-Za-z0-9.-]+)-", String.class, "-"),
        CHANNEL("%marker", "(?<CHANNEL>#.*?)\\s", String.class, " "), // Named group for channel, excluding the trailing whitespace
        USER("%msg", "(?<USER>.*?):", String.class, ": "),
        MESSAGE("%msg", "\\s(?<MESSAGE>.*)$", String.class, "");

        private final String pattern;
        private final String regex;
        private final String appendString;
        private final Class<?> patternClass;
        public final static String PATTERN_LAYOUT = "%d{yyy-MM-dd HH:mm:ss.SSS}{UTC}UTC %marker %msg%n";
        public final static String DATE_LAYOUT = "yyyy-MM-dd HH:mm:ss.SSS";

        LogPattern (String pattern, String regex, Class<?> patternClass, String appendString)
        {
            this.pattern = pattern;
            this.regex = regex;
            this.patternClass = patternClass;
            this.appendString = appendString;
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

        public String getAppendString ()
        {
            return appendString;
        }
    }

    public static Date parseDate (String dateString)
    {
        dateString = dateString.trim();
        // Step 1: Define the DateTimeFormatter with the pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        TemporalAccessor temporalAccessor = null;
        try{
        // Step 2: Parse the string into a TemporalAccessor object using the formatter
            temporalAccessor = formatter.parse(dateString.replace("UTC", ""));
        } catch (Exception exc)
        {
            System.out.println(exc);
        }

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

    // Method to format Date object to string in UTC
    public static String formatDateToString(Date date) {
        // Define the DateTimeFormatter with the pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        // Convert the Date object to LocalDateTime
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // Convert the LocalDateTime to UTC time zone
        LocalDateTime localDateTimeInUtc = localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();

        // Format the LocalDateTime to string
        return formatter.format(localDateTimeInUtc);
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

    public static Map<String, Object> parseLogLineFull (String logLine) {
        logLine = logLine.trim();
        Map<String, Object> parsedValues = new HashMap<>();

        StringBuilder combinedRegexBuilder = new StringBuilder();
        combinedRegexBuilder.append(LogPattern.DATE.getRegex());
        combinedRegexBuilder.append(LogPattern.SERVER.getRegex());
        combinedRegexBuilder.append(LogPattern.CHANNEL.getRegex());
        combinedRegexBuilder.append(LogPattern.USER.getRegex());
        combinedRegexBuilder.append(LogPattern.MESSAGE.getRegex());
        String combinedRegex = combinedRegexBuilder.toString();

        Pattern regexPattern = Pattern.compile(combinedRegex);
        Matcher matcher = regexPattern.matcher(logLine);
        while (matcher.find()) {
            for (LogPattern pattern : LogPattern.values()) {
                if (matcher.group(pattern.toString()) != null) {
                    String match = matcher.group(pattern.getMatchGroup());
                    switch (pattern.getPatternClass().getSimpleName()) {
                        case "Date":
                            parsedValues.put(pattern.toString(), parseDate(match));
                            break;
                        default:
                            parsedValues.put(pattern.toString(), match);
                            break;
                    }
                }
            }
        }

        return parsedValues;
    }
}
