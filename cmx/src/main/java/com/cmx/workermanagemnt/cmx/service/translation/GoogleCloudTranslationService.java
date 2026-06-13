package com.cmx.workermanagemnt.cmx.service.translation;

import java.util.List;
import java.util.Locale;

import org.springframework.util.StringUtils;

import com.cmx.workermanagemnt.cmx.config.TranslationProperties;
import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.domain.WorkerSkills;
import com.cmx.workermanagemnt.cmx.exception.TranslationException;
import com.cmx.workermanagemnt.cmx.service.TranslationService;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRegistrationRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerResponse;

public class GoogleCloudTranslationService implements TranslationService {

	private final GoogleTranslationClient client;
	private final TranslationProperties properties;

	public GoogleCloudTranslationService(GoogleTranslationClient client, TranslationProperties properties) {
		this.client = client;
		this.properties = properties;
	}

	@Override
	public WorkerRegistrationRequest toEnglish(WorkerRegistrationRequest request, Locale sourceLocale) {
		if (TranslatableWorkerFields.isEnglish(sourceLocale)) {
			return request;
		}
		WorkerRegistrationRequest copy = TranslatableWorkerFields.copyRequest(request);
		List<String> texts = TranslatableWorkerFields.collectFromRequest(copy);
		if (texts.isEmpty()) {
			return copy;
		}
		try {
			List<String> translated = client.translateBatch(texts, sourceLocale.getLanguage(), "en");
			TranslatableWorkerFields.applyToRequest(copy, translated);
			return copy;
		}
		catch (TranslationException ex) {
			if (properties.getOnFailure().shouldFailOnWrite()) {
				throw ex;
			}
			return copy;
		}
	}

	@Override
	public WorkerResponse fromEnglish(WorkerBasicInfo basicInfo, WorkerSkills skills, Locale targetLocale) {
		if (TranslatableWorkerFields.isEnglish(targetLocale)) {
			return WorkerResponse.fromDomain(basicInfo, skills);
		}
		WorkerBasicInfo localizedBasic = TranslatableWorkerFields.copyBasicInfo(basicInfo);
		WorkerSkills localizedSkills = TranslatableWorkerFields.copySkills(skills);
		List<String> texts = TranslatableWorkerFields.collectFromDomain(localizedBasic, localizedSkills);
		if (texts.isEmpty()) {
			return WorkerResponse.fromDomain(localizedBasic, localizedSkills);
		}
		try {
			List<String> translated = client.translateBatch(texts, "en", targetLocale.getLanguage());
			TranslatableWorkerFields.applyToBasicInfo(localizedBasic, translated);
			int basicFieldCount = TranslatableWorkerFields.countBasicInfoFields(basicInfo);
			TranslatableWorkerFields.applyToSkills(localizedSkills, translated, basicFieldCount);
			return WorkerResponse.fromDomain(localizedBasic, localizedSkills);
		}
		catch (TranslationException ex) {
			if (properties.getOnFailure().shouldFailOnRead()) {
				throw ex;
			}
			return WorkerResponse.fromDomain(basicInfo, skills);
		}
	}
}
