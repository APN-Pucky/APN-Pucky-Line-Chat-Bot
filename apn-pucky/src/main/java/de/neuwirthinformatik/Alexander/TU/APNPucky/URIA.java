package de.neuwirthinformatik.Alexander.TU.APNPucky;

import java.net.URI;

import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.action.URIAction.AltUri;

public class URIA {
	public static URIAction create(String message, String uri) {
		return create(message, URI.create(uri));
	}

	public static URIAction create(String message, URI uri) {
		return new URIAction(message, uri, null);
	}

}
