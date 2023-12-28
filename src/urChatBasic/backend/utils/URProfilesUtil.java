package urChatBasic.backend.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.stream.Collectors;
import urChatBasic.base.Constants;

public class URProfilesUtil {

        /**
     * Retrieves all profiles that have been created and returns a String[] of the names. If no profiles are available, it creates the "Default" profile and sets it as the
     * default.
     * @return
     */
    public static String[] getProfiles ()
    {
        List<String> allProfiles = new ArrayList<String>();
        try
        {
            if (Constants.BASE_PREFS.childrenNames().length == 0)
            {
                Constants.BASE_PREFS.put(Constants.KEY_DEFAULT_PROFILE_NAME, Constants.DEFAULT_PROFILE_NAME);
                allProfiles = Arrays.asList(new String[] {Constants.DEFAULT_PROFILE_NAME});
            } else {
                allProfiles = Arrays.stream(Constants.BASE_PREFS.childrenNames()).collect(Collectors.toList());
            }
        } catch (BackingStoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Use a Set, then convert to Array to drop any duplicates
        String[] profileNames = (new HashSet<>(allProfiles)).toArray(String[]::new);

        return profileNames;
    }
}
