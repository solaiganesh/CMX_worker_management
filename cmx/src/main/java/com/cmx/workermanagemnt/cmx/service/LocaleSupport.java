package com.cmx.workermanagemnt.cmx.service;

import java.util.Locale;

public final class LocaleSupport {

	private LocaleSupport() {
	}

	public static Locale resolveLocale(String languageHeader) {
		if (languageHeader == null || languageHeader.isBlank()) {
			return Locale.ENGLISH;
		}
		String primary = languageHeader.split(",")[0].trim();
		if (primary.isBlank()) {
			return Locale.ENGLISH;
		}
		return Locale.forLanguageTag(primary.replace('_', '-'));
	}

	public static boolean isEnglish(Locale locale) {
		return locale == null || locale.getLanguage().isBlank() || "en".equalsIgnoreCase(locale.getLanguage());
	}
}
