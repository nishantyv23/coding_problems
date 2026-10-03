import java.io.*;
import java.util.*;

public class CardRangeObfuscation
{
    private static final long RANGE_START = 0L;
    private static final long RANGE_END = 9_999_999_999L;

    static class Interval
    {
        long start;
        long end;
        String brand;

        Interval(long start, long end, String brand)
        {
            this.start = start;
            this.end = end;
            this.brand = brand;
        }
    }

    public static void main(String[] args) throws Exception
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String bin = br.readLine().trim();

        int n = Integer.parseInt(br.readLine().trim());

        List<Interval> intervals = new ArrayList<>();

        for (int i = 0; i < n; i++)
        {
            String line = br.readLine();
            String[] parts = line.split(",");

            long start = Long.parseLong(parts[0]);
            long end = Long.parseLong(parts[1]);
            String brand = parts[2];

            intervals.add(new Interval(start, end, brand));
        }

        // Sort intervals by their starting offset.
        intervals.sort(Comparator.comparingLong(interval -> interval.start));

        // Fill all gaps.
        List<Interval> result = fillGaps(intervals);

        // Convert offsets to complete 16-digit card numbers.
        StringBuilder output = new StringBuilder();

        for (Interval interval : result)
        {
            String fullStart = bin + String.format("%010d", interval.start);
            String fullEnd = bin + String.format("%010d", interval.end);

            output.append(fullStart).append(",").append(fullEnd).append(",").append(interval.brand).append("\n");
        }

        System.out.print(output);
    }

    private static List<Interval> fillGaps(List<Interval> intervals)
    {
        List<Interval> result = new ArrayList<>();

        // There is no brand available to fill the range,
        // so there is nothing meaningful we can return.
        if (intervals.isEmpty())
        {
            return result;
        }

        // Handle gap at the beginning.
        // Extend the first interval backwards to 0.
        Interval first = intervals.get(0);

        if (first.start > RANGE_START)
        {
            result.add(new Interval(RANGE_START, first.start - 1, first.brand));
        }

        // Process intervals in sorted order.
        Interval previous = null;

        for (Interval current : intervals)
        {
            if (previous != null)
            {
                // There is a gap if:
                // previous.end + 1 < current.start
                if (previous.end + 1 < current.start)
                {
                    // Fill the gap using the previous interval's brand.
                    result.add(new Interval(previous.end + 1, current.start - 1, previous.brand));
                }
            }

            result.add(new Interval(current.start, current.end, current.brand));

            previous = current;
        }

        // Handle gap at the end.
        // Extend the last interval to 9999999999.
        if (previous.end < RANGE_END)
        {
            result.add(new Interval(previous.end + 1, RANGE_END, previous.brand));
        }

        return result;
    }
}