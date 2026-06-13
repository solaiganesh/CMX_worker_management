package com.cmx.workermanagemnt.cmx.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class PropertiesLoader {

	private PropertiesLoader() {
	}

	public static Map<String, String> load(InputStream inputStream) throws IOException {
		Properties properties = new Properties();
		properties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		Map<String, String> values = new HashMap<>();
		for (String key : properties.stringPropertyNames()) {
			values.put(key, properties.getProperty(key));
		}
		return values;
	}
}
