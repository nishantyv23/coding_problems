import java.io.*;
import java.util.*;

public class AtlasCompanyNameCheck
{
    // normalized name -> account that currently owns the name
    // If a name exists in this map, it is unavailable.
    private static final Map<String, String> nameOwners = new HashMap<>();

    private static final Set<String> COMPANY_SUFFIXES = new HashSet<>(
            Arrays.asList("inc", "inc.", "corp", "corp.", "llc", 
                    "llc.", "l.l.c", "l.l.c.")
    );

    public static void main(String[] args) throws Exception
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Number of already registered companies.
        // For Part 3, these need to include the account ID
        // because ownership is required for reclamation.
        // Format: account_id|company_name
        int n = Integer.parseInt(br.readLine());

        for (int i = 0; i < n; i++)
        {
            String line = br.readLine();
            int separator = line.indexOf('|');

            String accountId = line.substring(0, separator);
            String companyName = line.substring(separator + 1);

            String normalized = normalize(companyName);

            if (!normalized.isEmpty())
            {
                nameOwners.put(normalized, accountId);
            }
        }

        // Number of requests.
        int m = Integer.parseInt(br.readLine());
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < m; i++)
        {
            String request = br.readLine();

            if (request.startsWith("RECLAIM,"))
            {
                processReclaim(request, output);
            }
            else
            {
                processRegistration(request, output);
            }
        }

        System.out.print(output);
    }

    /**
     * Process a normal name registration request.
     *
     * Format:
     *
     * account_id|proposed_name
     */
    private static void processRegistration(
            String request, StringBuilder output)
    {
        int separator = request.indexOf('|');

        String accountId = request.substring(0, separator);
        String proposedName = request.substring(separator + 1);

        String normalized = normalize(proposedName);

        // Empty normalized names are always unavailable.
        if (normalized.isEmpty())
        {
            output.append(accountId).append("|Name Not Available\n");
            return;
        }

        String existingOwner = nameOwners.putIfAbsent(normalized, accountId);
        if (existingOwner == null)
        {
            output.append(accountId).append("|Name Available\n");
        }
        else
        {
            output.append(accountId).append("|Name Not Available\n");
        }
    }

    /**
     * Process a reclamation request.
     *
     * Format:
     *
     * RECLAIM,account_id,original_proposed_name
     *
     * The company name may itself contain commas, so we only
     * treat the first two commas as separators.
     */
    private static void processReclaim(String request, StringBuilder output)
    {
        int firstComma = request.indexOf(',');
    
        int secondComma = request.indexOf(',', firstComma + 1);

        if (firstComma == -1 || secondComma == -1)
        {
            return;
        }

        String accountId = request.substring(firstComma + 1, secondComma);

        // This is the EXACT original proposed name.
        // We normalize it using the same normalization rules as registration.
        String originalName = request.substring(secondComma + 1);
        String normalized = normalize(originalName);

        // Empty normalized names cannot be registered,
        // so there is nothing to reclaim.
        if (normalized.isEmpty())
        {
            output.append(accountId).append("|Name Not Reclaimed\n");
            return;
        }

        // Find the account that currently owns the name.
        String owner = nameOwners.get(normalized);

        // Name doesn't exist.
        if (owner == null)
        {
            output.append(accountId).append("|Name Not Reclaimed\n");
            return;
        }

        // Only the original registering account can reclaim it.
        if (!owner.equals(accountId))
        {
            output.append(accountId).append("|Name Not Reclaimed\n");
            return;
        }

        // Authorized reclamation.
        // Removing the name makes it available for a future registration request.
        nameOwners.remove(normalized);

        output.append(accountId).append("|Name Reclaimed\n");
    }

    /**
     * Normalize a company name according to the Atlas rules.
     */
    private static String normalize(String name)
    {
        if (name == null)
        {
            return "";
        }

        // 1. Ignore case.
        String s = name.toLowerCase(Locale.ROOT);

        // 2. Treat '&' and ',' as spaces.
        s = s.replace('&', ' ');
        s = s.replace(',', ' ');

        // 3. Collapse consecutive whitespace.
        s = s.trim().replaceAll("\\s+", " ");

        if (s.isEmpty())
        {
            return "";
        }

        String[] words = s.split(" ");

        int start = 0;
        int end = words.length;

        // 4. Ignore leading "The", "An", or "A".
        if (words[0].equals("the") || words[0].equals("an") || words[0].equals("a"))
        {
            start++;
        }

        // The name consisted only of "The", "An", or "A".
        if (start >= end)
        {
            return "";
        }

        // 5. Ignore company suffix.
        if (isCompanySuffix(words[end - 1]))
        {
            end--;
        }

        // The name consisted only of a prefix and suffix.
        // Example: "The Inc."
        if (start >= end)
        {
            return "";
        }

        // 6. Ignore "And" unless it appears at the start.
        StringBuilder result = new StringBuilder();

        for (int i = start; i < end; i++)
        {
            String word = words[i];

            // Remove "and" when it is NOT the first meaningful word.
            if (word.equals("and") && i != start)
            {
                continue;
            }

            if (result.length() > 0)
            {
                result.append(' ');
            }

            result.append(word);
        }

        return result.toString();
    }

    private static boolean isCompanySuffix(String word)
    {
        return COMPANY_SUFFIXES.contains(word);
    }
}