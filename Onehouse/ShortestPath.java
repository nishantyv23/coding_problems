package Onehouse;

import java.util.*;

class ShortestPath
{
    static int shortestPathCalc(String start, String end, List<String> words)
    {
        if (start.equals(end))
            return 1;

        if (start.length() != end.length())
            return 0;

        Set<String> set = new HashSet<>(words);
        if (!set.contains(end))
            return 0;

        Queue<List<String>> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(List.of(start));
        visited.add(start);

        while (!queue.isEmpty())
        {
            List<String> currentPath = queue.poll();
            String currentWord = currentPath.get(currentPath.size() - 1);
            char[] chars = currentWord.toCharArray();

            for (int i = 0; i < chars.length; i++)
            {
                char originalChar = chars[i];

                for (char ch = 'a'; ch <= 'z'; ch++)
                {
                    if (ch == originalChar)
                        continue;

                    chars[i] = ch;
                    String nextWord = new String(chars);

                    if (nextWord.equals(end))
                        return currentPath.size();

                    if (set.contains(nextWord) && !visited.contains(nextWord))
                    {
                        visited.add(nextWord);
                        List<String> nextPath = new ArrayList<>(currentPath);
                        nextPath.add(nextWord);
                        queue.add(nextPath);
                    }
                }

                chars[i] = originalChar;
            }
        }
        return 0;
    }

    public static void main(String args[])
    {
        List<String> list = new ArrayList<>();
        list.add("safe");
        list.add("sack");
        list.add("hock");
        list.add("sock");
        list.add("soce");
        list.add("sofe");

        System.out.println(shortestPathCalc("lock", "open", list)); // 0
        System.out.println(shortestPathCalc("hack", "safe", list)); // 5
        list.add("sace");
        System.out.println(shortestPathCalc("hack", "safe", list)); // 3
    }
}