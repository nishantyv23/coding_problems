import java.util.HashMap;
import java.util.Map;

public class LoadBalancer {
    private final int numServers;
    private final int[] connectionCounts;
    private final Map<String, Integer> connectionToServer;
    private final Map<String, Integer> objectToServer;

    public LoadBalancer(int numServers) {
        this.numServers = numServers;
        // 1-indexed array for servers (size numServers + 1)
        this.connectionCounts = new int[numServers + 1];
        this.connectionToServer = new HashMap<>();
        this.objectToServer = new HashMap<>();
    }

    public int handleConnect(String connectionId, String userId, String objectId) {
        int targetServer;

        // Part 3: Check sticky routing first if objectId is provided and valid
        if (objectId != null && !objectId.isEmpty() && objectToServer.containsKey(objectId)) {
            targetServer = objectToServer.get(objectId);
        } else {
            // Part 1: Basic Load Balancing Rule
            // Select server with smallest connection count; break ties with smallest index
            int minConnections = Integer.MAX_VALUE;
            targetServer = 1;

            for (int i = 1; i <= numServers; i++) {
                if (connectionCounts[i] < minConnections) {
                    minConnections = connectionCounts[i];
                    targetServer = i;
                }
            }

            // Bind objectId permanently if provided
            if (objectId != null && !objectId.isEmpty()) {
                objectToServer.put(objectId, targetServer);
            }
        }

        // Update state
        connectionCounts[targetServer]++;
        connectionToServer.put(connectionId, targetServer);

        // Output log matching the exact expected format: connectionId,userId,targetIndex
        System.out.println(connectionId + "," + userId + "," + targetServer);
        return targetServer;
    }

    public void handleDisconnect(String connectionId) {
        // Part 2: Handle DISCONNECT requests and safely ignore invalid IDs
        if (connectionToServer.containsKey(connectionId)) {
            int serverIndex = connectionToServer.get(connectionId);
            connectionCounts[serverIndex]--;
            connectionToServer.remove(connectionId);
        }
    }

    // --- Example Test Driver ---
    public static void main(String[] args) {
        LoadBalancer lb = new LoadBalancer(3);

        // Simulating requests
        lb.handleConnect("conn1", "userA", "obj123"); // Goes to server 1
        lb.handleConnect("conn2", "userB", "obj456"); // Goes to server 2
        lb.handleConnect("conn3", "userC", "obj123"); // Sticky routing -> reuses server 1

        // Disconnect request
        lb.handleDisconnect("conn1");

        // New connection after disconnect
        lb.handleConnect("conn4", "userD", "obj789"); // Goes to server 1 (lower load)
    }
}