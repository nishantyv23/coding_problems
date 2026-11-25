package Onehouse;

import java.util.*;

public class ColorfulPaths
{
    public static int countColorfulPaths(int[][] matrix)
    {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
            return 0;
        
        Set<Integer> visitedColors = new HashSet<>();
        return backtrack(0, 0, matrix, visitedColors);
    }

    private static int backtrack(int r, int c, int[][] matrix, Set<Integer> visitedColors)
    {
        int n = matrix.length;
        int m = matrix[0].length;
        int currentColor = matrix[r][c];

        // If the color is already present in the current path, prune this branch
        if (visitedColors.contains(currentColor))
            return 0;

        // If we reached the bottom-right corner, we found a valid colorful path
        if (r == n - 1 && c == m - 1)
            return 1;

        // Add current color to the path
        visitedColors.add(currentColor);

        int totalPaths = 0;

        // Try moving Right
        if (c + 1 < m)
            totalPaths += backtrack(r, c + 1, matrix, visitedColors);

        // Try moving Down
        if (r + 1 < n)
            totalPaths += backtrack(r + 1, c, matrix, visitedColors);

        // Backtrack: remove the current color before returning up the stack
        visitedColors.remove(currentColor);

        return totalPaths;
    }

    public static void main(String[] args)
    {
        int[][] grid = {
            {1, 2, 3},
            {4, 1, 2},
            {5, 6, 7}
        };

        int result = countColorfulPaths(grid);
        System.out.println("Number of valid colorful paths: " + result);
    }
}