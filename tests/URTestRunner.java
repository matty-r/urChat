import java.util.ArrayList;
import java.util.List;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import backend.DialogTests;
import backend.MessageHandlerTests;
import backend.UserGUITests;


public class URTestRunner  {

    private static List<Class> testClasses;
    private static List<Failure> testFailures;

    public static void main (String[] args)
    {
        testFailures = new ArrayList<>();
        testClasses = new ArrayList<>();

        testClasses.add(DialogTests.class);
        testClasses.add(MessageHandlerTests.class);
        testClasses.add(UserGUITests.class);


        int totalRuns = 0;
        int totalFails = 0;

        for (Class<?> testClass : testClasses) {
            Result result = JUnitCore.runClasses(testClass);
            totalRuns += result.getRunCount();
            totalFails += result.getFailureCount();

            for(Failure failure : result.getFailures())
            {
                testFailures.add(failure);
            }
        }

        System.out.println("Total number of tests " + totalRuns);
        System.out.println("Total number of tests failed " + totalFails);

        for(Failure failure : testFailures)
        {
            System.out.println(failure.getMessage());
        }

        // TestRunner doesn't exit when the tests are complete for some reason
        System.exit(0);
    }
}
