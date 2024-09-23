package com.udf;
/*
------------------------------------------------------------------------------
  Name     : Udf.base.ALGO
  Purpose  : Algorithm
  Author   : Adam
  Revisions:
  Ver        Date        Author           Description
  ---------  ----------  ---------------  ------------------------------------
  1.0        2024/2/7    Adam             levenshtein

 format:
    property:
    method  : levenshtein
------------------------------------------------------------------------------
*/
public class ALGO {
    public static final String VERSION = "v1.0.0";

    public static int levenshtein (String str1, String str2) {
        if (str1 == null || str1.equals("")) return str2.length();
        if (str2 == null || str2.equals("")) return str1.length();
        if (str1.equals(str2)) return 0;

        // less space used - <<
        if (str1.length() > str2.length()) return levenshtein(str2, str1);
        // less space used - >>

        int m, n, d, i, j;
        m = str1.length();
        n = str2.length();
        int[][] dp = new int[m+1][2];
        for (i = 0; i<=m; i++) dp[i][0] = i;
        dp[0][1] = 1;

        for (j = 1; j<=n; j++) {
            for (i = 1; i<=m; i++) {
                d = (str1.charAt(i-1) == str2.charAt(j-1)) ? 0 : 1;
                dp[i][1] = Math.min(dp[i-1][0]+d, Math.min(dp[i][0]+1, dp[i-1][1]+1));
                //System.out.println(dp[i][1]);
            }
            for (i = 0; i<=m; i++) {
                dp[i][0] = dp[i][1];
            }
            dp[0][1] = dp[0][0] + 1;
        }

        return dp[m][1];
    }

    public static int levenshtein2 (String str1, String str2) {
        if (str1 == null || str1.equals("")) return str2.length();
        if (str2 == null || str2.equals("")) return str1.length();
        if (str1.equals(str2)) return 0;

        int m, n, d, i, j;
        m = str1.length();
        n = str2.length();
        int[][] dp = new int[m+1][n+1];
        for (i = 0; i<=m; i++) dp[i][0] = i;
        for (j = 1; j<=n; j++) dp[0][j] = j;

        for (i = 1; i<=m; i++) {
            for (j = 1; j<=n; j++) {
                d = (str1.charAt(i-1) == str2.charAt(j-1)) ? 0 : 1;
                dp[i][j] = Math.min(dp[i-1][j-1]+d, Math.min(dp[i][j-1]+1, dp[i-1][j]+1));
                //System.out.println(dp[i][j]);
            }
        }

        return dp[m][n];
    }
}
