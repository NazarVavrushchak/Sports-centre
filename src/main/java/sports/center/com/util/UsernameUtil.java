package sports.center.com.util;

import sports.center.com.dao.GenericDao;
import sports.center.com.model.User;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UsernameUtil {
    public static <T extends User> String generateUsername(String firstName, String lastName, GenericDao<T> userDao) {
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentException("First name and last name cannot be null");
        }
        String baseUsername = (firstName.trim() + "." + lastName.trim()).toLowerCase();

        Set<String> existingUsernames = userDao.findAll().stream()
                .map(user -> user.getUsername().toLowerCase())
                .filter(name -> name.startsWith(baseUsername))
                .collect(Collectors.toSet());

        Pattern pattern = Pattern.compile("^" + Pattern.quote(baseUsername) + "(\\d*)$");

        Set<Integer> usedIndexes = existingUsernames.stream()
                .map(name -> {
                    Matcher matcher = pattern.matcher(name);
                    if (matcher.matches()) {
                        return matcher.group(1).isEmpty() ? 0 : Integer.parseInt(matcher.group(1));
                    }
                    return null;
                })
                .filter(index -> index != null)
                .collect(Collectors.toSet());

        int nextIndex = usedIndexes.isEmpty() ? 0 : usedIndexes.stream().max(Integer::compareTo).get() + 1;

        return nextIndex == 0 ? baseUsername : baseUsername + nextIndex;
    }
}