package urChatBasic.frontend.utils;

import java.awt.Color;

public class URColour {

    public static boolean useDarkColour(Color sourceColour)
    {
        // Counting the perceptive luminance - human eye favors green color...
        double luminance = (0.299 * sourceColour.getRed() + 0.587 * sourceColour.getGreen() + 0.114 * sourceColour.getBlue()) / 255;

        if (luminance > 0.5)
            return true; // bright colors - black font
        else
            return false; // dark colors - white font
    }

    public static Color getContrastColour(Color sourceColor)
    {
        if(useDarkColour(sourceColor))
        {
            return new Color(0,0,0);
        } else {
            return new Color(255,255,255);
        }
    }

    public static Color getInvertedColour(Color sourceColour) {
        int r = 0;
        int g = 0;
        int b = 0;

        // Invert color components
        r = 255 - sourceColour.getRed();
        g = 255 - sourceColour.getGreen();
        b = 255 - sourceColour.getBlue();

        // Create and return a new Color object
        return new Color(r, g, b);
    }
}
