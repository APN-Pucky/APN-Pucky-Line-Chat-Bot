package com.example.bot.spring;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class Wget {

	public static String wGet(String saveAsFile, String urlOfFile) {
		String ret = null;
		InputStream httpIn = null;
		OutputStream fileOutput = null;
		OutputStream bufferedOut = null;
		try {
			// check the http connection before we do anything to the fs
			httpIn = new BufferedInputStream(new URL(urlOfFile).openStream());
			// prep saving the file
			ret = StreamUtil.readFullyAsString(httpIn,"UTF-8");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (bufferedOut != null) {
					bufferedOut.close();
				}
				if (fileOutput != null) {
					fileOutput.close();
				}
				if (httpIn != null) {
					httpIn.close();
				}
			} catch (IOException e) {
				return null;
			}
		}
		return ret;
	}
}
