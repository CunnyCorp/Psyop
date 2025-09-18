package monster.psyop.client.utility;

import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;
import lombok.SneakyThrows;
import monster.psyop.client.Liberty;
import monster.psyop.client.config.gui.CoreConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class StringUtils {
    public static final Random RANDOM = new Random(System.currentTimeMillis());
    private static final List<String> alphabet =
            Arrays.stream("qwertyuiopasdfghjklzxcvbnm1234567890/._-".split("")).toList();
    private static final ObjectBigArrayBigList<String> wordList = new ObjectBigArrayBigList<>();
    private static final Map<String, String> owoMap = new HashMap<>();
    private static final Path WORDS = PathIndex.CLIENT.resolve("words.txt");
    private static Long worldListStart = 0L;

    public static final List<String> CASE_STYLES = Arrays.asList(
            "original", "title", "sentence", "lowercase", "uppercase", "pascal"
    );

    /**
     * Format text according to the configured style options
     */
    public static String formatText(String text, CoreConfig config) {
        if (text == null || text.isEmpty()) return text;

        String formatted = text;

        // Apply casing style first
        String caseStyle = config.textCaseStyle.get().toLowerCase();
        switch (caseStyle) {
            case "title":
                formatted = toTitleCase(formatted);
                break;
            case "sentence":
                formatted = toSentenceCase(formatted);
                break;
            case "lowercase":
                formatted = formatted.toLowerCase(Locale.ROOT);
                break;
            case "uppercase":
                formatted = formatted.toUpperCase(Locale.ROOT);
                break;
            case "pascal":
                formatted = toPascalCase(formatted);
                break;
            default: // "original" - do nothing
                break;
        }

        // Apply word separation styles (mutually exclusive)
        if (config.useSnakeCase.get()) {
            formatted = toSnakeCase(formatted);
        } else if (config.useCamelCase.get()) {
            formatted = toCamelCase(formatted);
        } else if (config.useKebabCase.get()) {
            formatted = toKebabCase(formatted);
        } else if (config.usePascalCase.get()) {
            formatted = toPascalCase(formatted);
        }

        return formatted;
    }

    public static String toTitleCase(String text) {
        if (text == null || text.isEmpty()) return text;

        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase(Locale.ROOT))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    public static String toSentenceCase(String text) {
        if (text == null || text.isEmpty()) return text;

        return Character.toUpperCase(text.charAt(0)) +
                text.substring(1).toLowerCase(Locale.ROOT);
    }

    public static String toSnakeCase(String text) {
        return text.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT);
    }

    public static String toCamelCase(String text) {
        if (text == null || text.isEmpty()) return text;

        String[] words = text.split("[\\s_\\-]+");
        StringBuilder result = new StringBuilder(words[0].toLowerCase(Locale.ROOT));

        for (int i = 1; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                result.append(Character.toUpperCase(words[i].charAt(0)))
                        .append(words[i].substring(1).toLowerCase(Locale.ROOT));
            }
        }

        return result.toString();
    }

    public static String toPascalCase(String text) {
        if (text == null || text.isEmpty()) return text;

        String[] words = text.split("[\\s_\\-]+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase(Locale.ROOT));
            }
        }

        return result.toString();
    }

    public static String toKebabCase(String text) {
        return text.trim()
                .replaceAll("\\s+", "-")
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Enhanced readable method that uses configuration
     */
    public static String readable(String original, CoreConfig config) {
        if (config == null) {
            return readable(original); // fallback to original method
        }
        return formatText(original, config);
    }

    public StringUtils() {
        Liberty.EXECUTOR.submit(StringUtils::loadWordList);
        owoMap.putAll(Map.of("hacker", "haxor", "hacks", "hax", "ch", "chw", "Qu", "Qwu", "qu", "qwu"));
        owoMap.putAll(
                Map.of(
                        "r", "w", "l", "w", "R", "W", "L", "W", "no", "nu", "has", "haz", "have", "haz", "you",
                        "uu", "the ", "da ", "The ", "Da "));
    }

    public static String readable(String original) {
        original = original.toLowerCase();
        StringBuilder newString = new StringBuilder();
        if (original.contains("-") || original.contains("_")) {
            for (String segment : original.split("[-_]")) {
                String first = segment.split("")[0];
                newString.append(segment.replaceFirst(first, first.toUpperCase(Locale.ROOT)));
            }
        } else {
            newString = new StringBuilder(original.toLowerCase(Locale.ROOT));
            String first = newString.toString().split("")[0];
            newString =
                    new StringBuilder(
                            newString.toString().replaceFirst(first, first.toUpperCase(Locale.ROOT)));
        }

        return newString.toString();
    }

    public static String owoify(String str) {
        var ref =
                new Object() {
                    String ph = str;
                };
        owoMap.forEach(
                (s, s2) -> {
                    if (ref.ph.contains(s)) ref.ph = ref.ph.replaceAll(s, s2);
                });
        return ref.ph;
    }

    public static boolean toBool(String string) {
        return switch (string) {
            case "true", "yes", "1", "y" -> true;
            default -> false;
        };
    }

    @SneakyThrows
    public static void loadWordList() {
        if (worldListStart == 0) worldListStart = System.nanoTime();

        if (Files.exists(WORDS) && Files.isRegularFile(WORDS)) {
            wordList.addAll(Files.readAllLines(WORDS));
            Liberty.LOG.info("Word List took {}ns to load", System.nanoTime() - worldListStart);
        } else {
            Files.write(
                    WORDS,
                    FileSystem.getUrl("https://raw.githubusercontent.com/dwyl/english-words/master/words.txt")
                            .getBytes(),
                    StandardOpenOption.CREATE_NEW);
            loadWordList();
        }
    }

    public static String randomText(int amount) {
        return randomText(amount, false);
    }

    public static String randomText(int amount, boolean uni) {
        StringBuilder str = new StringBuilder();

        if (uni) {
            int leftLimit = 10000;
            int rightLimit = 200000;

            for (int i = 0; i < amount; i++) {
                str.append((char) (leftLimit + (int) (RANDOM.nextFloat() * (rightLimit - leftLimit + 1))));
            }
        } else {
            for (int i = 0; i < amount; i++) {
                str.append(CollectionUtils.random(alphabet));
            }
        }

        return str.toString();
    }

    public static String randomInt(int amount) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            str.append(RANDOM.nextInt(9));
        }
        return str.toString();
    }

    public String randomWord() {
        String random;

        do {
            random = CollectionUtils.random(wordList);
        } while (random == null || random.isBlank());

        return random.strip();
    }

    public String randomWord(Predicate<String> filter) {
        String random;

        do {
            random = CollectionUtils.random(wordList);
        } while (random == null || random.isBlank() || !filter.test(random));

        return random.strip();
    }
}
