package com.cmx.workermanagemnt.cmx.service.translation;

import java.util.List;

public interface BatchTranslationClient {

	List<String> translateBatch(List<String> texts, String sourceLanguage, String targetLanguage);
}
