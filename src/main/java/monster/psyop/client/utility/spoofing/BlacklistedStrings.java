package monster.psyop.client.utility.spoofing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlacklistedStrings {
    public static List<String> BLACKLISTED_CONTAINS = new ArrayList<>();

    static {
        BLACKLISTED_CONTAINS.addAll(Arrays.stream(new String[]{"meteor-client", "liquidbounce", "wurst", "749", "xray", "freecam", "future", "rusherhack"}).toList());
    }

    public static boolean isBlacklisted(String str) {
        for (String blk : BLACKLISTED_CONTAINS) {
            if (str.toLowerCase().contains(blk)) {
                return true;
            }
        }

        return false;
    }
}
