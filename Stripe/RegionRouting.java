import java.io.*;
import java.util.*;

public class RegionRouting
{

    private static final double EARTH_RADIUS_KM = 6371.0;

    static class Region
    {
        String name;
        int latitude;
        int longitude;
        int capacity;
        int load;
        boolean healthy;

        Region(String name, int latitude, int longitude, int capacity)
        {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.capacity = capacity;
            this.load = 0;
            this.healthy = true;
        }

        boolean hasCapacity()
        {
            return load < capacity;
        }
    }

    static class Candidate
    {
        Region region;
        int distance;

        Candidate(Region region, int distance)
        {
            this.region = region;
            this.distance = distance;
        }
    }

    private final Map<String, Region> regions = new HashMap<>();

    private void register(String[] parts)
    {
        if (parts.length != 5)
        {
            System.out.println("ERROR");
            return;
        }

        String name = parts[1];
        int latitude;
        int longitude;
        int capacity;

        try
        {
            latitude = Integer.parseInt(parts[2]);
            longitude = Integer.parseInt(parts[3]);
            capacity = Integer.parseInt(parts[4]);
        }
        catch (NumberFormatException e)
        {
            System.out.println("ERROR");
            return;
        }

        if (!validLatitude(latitude) || !validLongitude(longitude))
        {
            System.out.println("ERROR");
            return;
        }

        if (capacity <= 0)
        {
            System.out.println("ERROR");
            return;
        }

        if (regions.containsKey(name))
        {
            System.out.println("ERROR");
            return;
        }

        regions.put(name, new Region(name, latitude, longitude, capacity));
        System.out.println("OK");
    }

    private void setHealth(String[] parts)
    {
        if (parts.length != 3)
        {
            System.out.println("ERROR");
            return;
        }

        String regionName = parts[1];
        Region region = regions.get(regionName);

        if (region == null)
        {
            System.out.println("ERROR");
            return;
        }

        region.healthy = Boolean.parseBoolean(parts[2]);
        System.out.println("OK");
    }

    private void distance(String[] parts)
    {
        if (parts.length != 5)
        {
            System.out.println("ERROR");
            return;
        }

        int lat1;
        int lon1;
        int lat2;
        int lon2;

        try
        {
            lat1 = Integer.parseInt(parts[1]);
            lon1 = Integer.parseInt(parts[2]);
            lat2 = Integer.parseInt(parts[3]);
            lon2 = Integer.parseInt(parts[4]);
        }
        catch (NumberFormatException e)
        {
            System.out.println("ERROR");
            return;
        }

        if (!validLatitude(lat1) || !validLatitude(lat2) || !validLongitude(lon1) || !validLongitude(lon2))
        {
            System.out.println("ERROR");
            return;
        }

        System.out.println(haversine(lat1, lon1, lat2, lon2));
    }

    private void route(String[] parts)
    {
        if (parts.length != 3)
        {
            System.out.println("ERROR");
            return;
        }

        int latitude;
        int longitude;

        try
        {
            latitude = Integer.parseInt(parts[1]);
            longitude = Integer.parseInt(parts[2]);
        }
        catch (NumberFormatException e)
        {
            System.out.println("ERROR");
            return;
        }

        if (!validLatitude(latitude) || !validLongitude(longitude))
        {
            System.out.println("ERROR");
            return;
        }

        List<Candidate> candidates = new ArrayList<>();

        for (Region region : regions.values())
        {
            if (!region.healthy)
            {
                continue;
            }

            int distance = haversine(latitude, longitude, region.latitude, region.longitude);
            candidates.add(new Candidate(region, distance));
        }

        /*
         * Sort:
         *   1. distance
         *   2. region name
         */
        candidates.sort((a, b) -> {
            if (a.distance != b.distance)
            {
                return Integer.compare(a.distance, b.distance);
            }
            return a.region.name.compareTo(b.region.name);
        });

        List<String> attempted = new ArrayList<>();

        for (Candidate candidate : candidates)
        {
            Region region = candidate.region;
            attempted.add(region.name);

            if (region.hasCapacity())
            {
                region.load++;
                System.out.println(region.name + " " + candidate.distance + " " + String.join(" ", attempted));
                return;
            }
        }

        /*
         * No healthy region has capacity.
         */
        System.out.println("NONE 0 " + String.join(" ", attempted));
    }

    private static boolean validLatitude(int latitude)
    {
        return latitude >= -90 && latitude <= 90;
    }

    private static boolean validLongitude(int longitude)
    {
        return longitude >= -180 && longitude <= 180;
    }

    private static int haversine(int lat1, int lon1, int lat2, int lon2)
    {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) Math.round(EARTH_RADIUS_KM * c);
    }

    public void processInput() throws Exception
    {
        BufferedReader br = new BufferedReader(
            new InputStreamReader(System.in)
        );
        String line;

        while ((line = br.readLine()) != null)
        {
            line = line.trim();

            if (line.isEmpty())
            {
                continue;
            }

            String[] parts = line.split("\\s+");

            switch (parts[0])
            {
                case "REGISTER":
                    register(parts);
                    break;

                case "SET_HEALTHZ":
                    setHealth(parts);
                    break;

                case "DISTANCE":
                    distance(parts);
                    break;

                case "ROUTE":
                    route(parts);
                    break;
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        RegionRouting router = new RegionRouting();
        router.processInput();
    }
}