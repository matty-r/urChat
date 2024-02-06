package utils;

import java.awt.Color;
import java.util.Random;

public class TestUtils {

    public static Color getRandomColour ()
    {
        int red = new Random().nextInt(256);
        int green = new Random().nextInt(256);
        int blue = new Random().nextInt(256);

        return new Color(red, green, blue);
    }
}
