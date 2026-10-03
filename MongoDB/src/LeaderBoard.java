import java.util.*;

class User {
    int id;
    String name;

    User(int id, String name) {
        this.id = id;
        this.name = name;
    }
}

class Login {
    int userId;
    int timestamp;

    Login(int userId, int timestamp) {
        this.userId = userId;
        this.timestamp = timestamp;
    }
}

public class LeaderBoard {

    public static void printLeaderboard(List<User> users, List<Login> logins) {

        // userId -> name
        Map<Integer, String> userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.id, user.name);
        }

        // userId -> unique login timestamps
        Map<Integer, Set<Integer>> loginMap = new HashMap<>();

        for (Login login : logins) {
            loginMap
                    .computeIfAbsent(login.userId, k -> new HashSet<>())
                    .add(login.timestamp);
        }

        // Convert map to list
        List<Map.Entry<Integer, Set<Integer>>> leaderboard =
                new ArrayList<>(loginMap.entrySet());

        // Sort by unique login count (descending)
        leaderboard.sort((a, b) ->
                Integer.compare(
                        b.getValue().size(),
                        a.getValue().size()));

        // Print leaderboard
        for (Map.Entry<Integer, Set<Integer>> entry : leaderboard) {
            System.out.println(
                    userMap.get(entry.getKey()) +
                    " : " +
                    entry.getValue().size());
        }
    }

    public static void main(String[] args) {

        List<User> users = Arrays.asList(
                new User(1, "qw"),
                new User(2, "er")
        );

        List<Login> logins = Arrays.asList(
                new Login(1, 1),
                new Login(1, 1),
                new Login(1, 3),
                new Login(2, 4),
                new Login(2, 5),
                new Login(1, 7)
        );

        printLeaderboard(users, logins);
    }
}