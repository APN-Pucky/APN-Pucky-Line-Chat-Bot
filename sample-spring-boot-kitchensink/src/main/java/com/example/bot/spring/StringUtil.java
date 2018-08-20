package com.example.bot.spring;

public class StringUtil {
	public static String capitalizeOnlyFirstLetters(String original) {
	    if (original == null || original.length() == 0) {
	        return original;
	    }
	    String[] words = original.split(" ");
	    String ret = "";
	    for(String w: words) ret+= capitalizeOnlyFirstLetter(w) + " ";
	    return removeLastCharacter(ret);
	}

	public static String removeLastCharacter(String o)
	{
		return removeLastCharacter(o,1);
	}

	public static String removeLastCharacter(String o,int x)
	{
		return o.substring(0,o.length()-x);
	}

		public static boolean equalsIgnoreSpecial(String a, String b)
		{
				String a1 = a.replaceAll("'", "").replaceAll("-","").replaceAll("\"", "").replaceAll(" ", "").replaceAll("\\.", "").replaceAll(",", "");
				String b1 = b.replaceAll("'", "").replaceAll("-","").replaceAll("\"", "").replaceAll(" ", "").replaceAll("\\.", "").replaceAll(",", "");
				return a1.equalsIgnoreCase(b1);
		}

		public static boolean containsIgnoreSpecial(String a, String b)
		{
				String a1 = a.replaceAll("'", "").replaceAll("-","").replaceAll("\"", "").replaceAll(" ", "").replaceAll("\\.", "").replaceAll(",", "").toLowerCase();
				String b1 = b.replaceAll("'", "").replaceAll("-","").replaceAll("\"", "").replaceAll(" ", "").replaceAll("\\.", "").replaceAll(",", "").toLowerCase();
				return a1.contains(b1);
		}

	public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)"+regex+"(?!.*?"+regex+")", replacement);
    }

	private static String capitalizeOnlyFirstLetter(String original) {
	    if (original == null || original.length() == 0) {
	        return original;
	    }
	    return original.substring(0, 1).toUpperCase() + original.substring(1).toLowerCase();
	}
}
