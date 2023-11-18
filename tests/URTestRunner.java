import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import backend.DialogTests;
import backend.MessageHandlerTests;
import backend.UserGUITests;


public class URTestRunner  {

    private static List<Class> testClasses;
    private static List<Failure> testFailures;

    public static void main (String[] args) throws IOException
    {
        testFailures = new ArrayList<>();
        testClasses = new ArrayList<>();

        testClasses.add(DialogTests.class);
        testClasses.add(MessageHandlerTests.class);
        testClasses.add(UserGUITests.class);

        // Create the report directory if it doesn't exist
        File reportDirectory = new File("report");
        if (!reportDirectory.exists()) {
            reportDirectory.mkdirs(); // Create directory and any missing parent directories
        }

        // Create a RunListener to output test results as XML
        RunListener listener = new JUnitXmlListener(new FileWriter("report/junit-report.xml"));

        // Create a Request object to encapsulate all test classes
        Request request = Request.classes(testClasses.toArray(new Class<?>[0]));

        int totalRuns = 0;
        int totalFails = 0;

        JUnitCore core = new JUnitCore();
        core.addListener(listener);

        // Run all tests together
        Result result = core.run(request);
        totalRuns += result.getRunCount();
        totalFails += result.getFailureCount();

        for (Failure failure : result.getFailures()) {
            testFailures.add(failure);
        }

        System.out.println("Total number of tests " + totalRuns);
        System.out.println("Total number of tests failed " + totalFails);

        for (Failure failure : testFailures) {
            System.out.println(failure.getMessage());
        }

        System.out.println("Done");

        System.exit(0);
    }

    static class JUnitXmlListener extends RunListener {
        private final FileWriter writer;

        public JUnitXmlListener(FileWriter writer) {
            this.writer = writer;
        }

        @Override
        public void testRunFinished (Result result) throws IOException {
            testRunFinishedXML(result);
        }

        public void testRunFinishedXML(Result result) throws IOException {
            writer.write("<testsuite name=\"JUnit Test Suite\" tests=\"" + result.getRunCount() +
                        "\" failures=\"" + result.getFailureCount() +
                        "\" time=\"" + result.getRunTime() + "\">");

            for (Failure failure : result.getFailures()) {
                writer.write("<testcase name=\"" + failure.getDescription().getMethodName() + "\">");
                writer.write("<failure message=\"" + failure.getMessage() + "\">" + failure.getTrace() + "</failure>");
                writer.write("</testcase>");
            }

            writer.write("</testsuite>");
            writer.flush();
            writer.close();
        }
    }
}
