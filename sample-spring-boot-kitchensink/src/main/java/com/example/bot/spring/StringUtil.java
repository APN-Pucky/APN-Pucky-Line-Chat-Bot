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


	private static String capitalizeOnlyFirstLetter(String original) {
	    if (original == null || original.length() == 0) {
	        return original;
	    }
	    return original.substring(0, 1).toUpperCase() + original.substring(1).toLowerCase();
	}
}
