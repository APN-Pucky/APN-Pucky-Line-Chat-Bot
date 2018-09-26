package com.example.bot.spring;

import java.util.Arrays;

public class StringUtil {
	public static String capitalizeOnlyFirstLetters(String original) {
		if (original == null || original.length() == 0) {
			return original;
		}
		String[] words = original.split(" ");
		String ret = "";
		for (String w : words)
			ret += capitalizeOnlyFirstLetter(w) + " ";
		return removeLastCharacter(ret);
	}
	static int calculate(String x, String y) {
	    int[][] dp = new int[x.length() + 1][y.length() + 1];

	    for (int i = 0; i <= x.length(); i++) {
	        for (int j = 0; j <= y.length(); j++) {
	            if (i == 0) {
	                dp[i][j] = j;
	            }
	            else if (j == 0) {
	                dp[i][j] = i;
	            }
	            else {
	                dp[i][j] = min(dp[i - 1][j - 1] 
	                 + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)), 
	                  dp[i - 1][j] + 1, 
	                  dp[i][j - 1] + 1);
	            }
	        }
	    }

	    return dp[x.length()][y.length()];
	}
	public static int min(int... numbers) {
        return Arrays.stream(numbers)
          .min().orElse(Integer.MAX_VALUE);
    }
	public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }
	public static String removeLastCharacter(String o) {
		return removeLastCharacter(o, 1);
	}
	public static int indexOfIgnoreCard(String a, String b) {
		return a.toLowerCase().indexOf(b.toLowerCase());
	}

	public static String removeLastCharacter(String o, int x) {
		return o.substring(0, o.length() - x);
	}

	public static boolean equalsIgnoreSpecial(String a, String b) {
		String a1 = a.replaceAll("'", "").replaceAll("-", "").replaceAll("\"", "").replaceAll(" ", "")
				.replaceAll("\\.", "").replaceAll(",", "");
		String b1 = b.replaceAll("'", "").replaceAll("-", "").replaceAll("\"", "").replaceAll(" ", "")
				.replaceAll("\\.", "").replaceAll(",", "");
		return a1.equalsIgnoreCase(b1);
	}

	public static boolean containsIgnoreSpecial(String a, String b) {
		String a1 = a.replaceAll("'", "").replaceAll("-", "").replaceAll("\"", "").replaceAll(" ", "")
				.replaceAll("\\.", "").replaceAll(",", "").toLowerCase();
		String b1 = b.replaceAll("'", "").replaceAll("-", "").replaceAll("\"", "").replaceAll(" ", "")
				.replaceAll("\\.", "").replaceAll(",", "").toLowerCase();
		return a1.contains(b1);
	}

	public static String replaceLast(String text, String regex, String replacement) {
		return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
	}

	private static String capitalizeOnlyFirstLetter(String original) {
		if (original == null || original.length() == 0) {
			return original;
		}
		return original.substring(0, 1).toUpperCase() + original.substring(1).toLowerCase();
	}
}
