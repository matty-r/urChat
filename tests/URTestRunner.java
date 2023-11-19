import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import backend.DialogTests;
import backend.MessageHandlerTests;
import backend.UserGUITests;
import java.util.ArrayList;
import java.util.List;
import org.testng.TestListenerAdapter;

public class URTestRunner {

    public static void main(String[] args) {
        // Create a TestNG instance
        TestNG testNG = new TestNG();

        // Define your test classes
        List<Class> classes = new ArrayList<>();
        classes.add(DialogTests.class);
        classes.add(MessageHandlerTests.class);
        classes.add(UserGUITests.class);
        // Add more test classes as needed

        // Set the test classes to TestNG
        testNG.setTestClasses(classes.toArray(new Class[0]));

        // Run TestNG
        TestListenerAdapter listener = new TestListenerAdapter();
        testNG.addListener(listener);

        testNG.run();

        // Get test results
        System.out.println("Total tests run: " + listener.getPassedTests().size());
        System.out.println("Total tests failed: " + listener.getFailedTests().size());

        System.exit(0);
    }
}