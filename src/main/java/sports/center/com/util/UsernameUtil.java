package sports.center.com.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UsernameUtil {
    private static final Logger logger = LoggerFactory.getLogger(UsernameUtil.class);

    private final EntityManager entityManager;

    public String generateUsername(String firstName, String lastName) {
        if (firstName == null || lastName == null || firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name and last name cannot be null");
        }

        String baseUsername = formatBaseUsername(firstName, lastName);
        Set<String> existingUsernames = getAllUsernames();
        Set<Integer> usedIndexes = extractUsedIndexes(existingUsernames, baseUsername);
        int nextIndex = calculateNextIndex(usedIndexes);
        String finalUsername = formatFinalUsername(baseUsername, nextIndex);

        logger.info("Generated username: {}", finalUsername);
        return finalUsername;
    }

    private String formatBaseUsername(String firstName, String lastName) {
        return (firstName.trim() + "." + lastName.trim()).toLowerCase();
    }

    private Set<Integer> extractUsedIndexes(Set<String> existingUsernames, String baseUsername) {
        Pattern pattern = Pattern.compile("^" + Pattern.quote(baseUsername) + "(\\d*)$");

        return existingUsernames.stream()
                .map(name -> parseIndexFromUsername(pattern, name))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Integer parseIndexFromUsername(Pattern pattern, String username) {
        Matcher matcher = pattern.matcher(username);
        if (matcher.matches()) {
            return matcher.group(1).isEmpty() ? 0 : Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private int calculateNextIndex(Set<Integer> usedIndexes) {
        return usedIndexes.isEmpty() ? 0 : usedIndexes.stream().max(Integer::compareTo).orElse(0) + 1;
    }

    private String formatFinalUsername(String baseUsername, int nextIndex) {
        return nextIndex == 0 ? baseUsername : baseUsername + nextIndex;
    }

    private Set<String> getAllUsernames() {
        TypedQuery<String> query = entityManager.createQuery(
                "SELECT u.username FROM User u", String.class);
        return query.getResultStream().collect(Collectors.toSet());
    }
}