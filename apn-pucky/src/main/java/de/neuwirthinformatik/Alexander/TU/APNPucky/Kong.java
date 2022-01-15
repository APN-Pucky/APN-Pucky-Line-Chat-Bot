package de.neuwirthinformatik.Alexander.TU.APNPucky;

public class Kong {

	public static String getRoadMapUrl() {
		String general = Wget.wGet("https://www.kongregate.com/forums/2468-general");
		String[] lines = general.split("\n");
		String fin = "";
		for (String l : lines) {
			if (l.contains("Roadmap") || l.contains("Showdown at Avalon")) {
				fin = l;
				break;
			}
		}
		String[] guesses = new String[] {"Dev","ComDev", "COMDEV","DEV"};
		String url = "";
		try {
			for(String g : guesses) {
				if(!url.equals(""))break;
				url = "https://www.kongregate.com"
						+ fin.substring(fin.indexOf("href=\"/forums/2468-general") + 6, fin.indexOf("\">["+g+"]"));
			}
		}
		catch(Exception e) {

			url = "https://www.kongregate.com"
					+ fin.substring(fin.indexOf("href=\"/forums/2468-general") + 6, fin.indexOf("\">[COMDEV]"));
		}
		return url;
	}

	public static String getFirstKongPost(String url) {
		String road = Wget.wGet(url);

		String map = road.substring(road.indexOf("<div class=\"raw_post\""));
		map = map.substring(map.indexOf(">") + 1);
		map = map.substring(0, map.indexOf("</div>"));
		return map;
	}

	public static String getRoadMap(String url) {
		return getFirstKongPost(url);
	}

	public static String getRoadMap() {
		return getRoadMap(getRoadMapUrl());
	}
}
