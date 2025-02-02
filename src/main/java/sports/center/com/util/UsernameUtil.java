package sports.center.com.util;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UsernameUtil {

    public static String generateUsername(String firstName, String lastName, Set<String> existingUsernames) {
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentException("First name and last name cannot be null");
        }

        String baseUsername = formatBaseUsername(firstName, lastName);
        Set<Integer> usedIndexes = extractUsedIndexes(existingUsernames, baseUsername);
        int nextIndex = calculateNextIndex(usedIndexes);

        return formatFinalUsername(baseUsername, nextIndex);
    }

    private static String formatBaseUsername(String firstName, String lastName) {
        return (firstName.trim() + "." + lastName.trim()).toLowerCase();
    }

    private static Set<Integer> extractUsedIndexes(Set<String> existingUsernames, String baseUsername) {
        Pattern pattern = Pattern.compile("^" + Pattern.quote(baseUsername) + "(\\d*)$");
        return existingUsernames.stream()
                .map(name -> parseIndexFromUsername(pattern, name))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private static Integer parseIndexFromUsername(Pattern pattern, String username) {
        Matcher matcher = pattern.matcher(username);
        if (matcher.matches()) {
            return matcher.group(1).isEmpty() ? 0 : Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private static int calculateNextIndex(Set<Integer> usedIndexes) {
        return usedIndexes.isEmpty() ? 0 : usedIndexes.stream().max(Integer::compareTo).orElse(0) + 1;
    }

    private static String formatFinalUsername(String baseUsername, int nextIndex) {
        return nextIndex == 0 ? baseUsername : baseUsername + nextIndex;
    }
}