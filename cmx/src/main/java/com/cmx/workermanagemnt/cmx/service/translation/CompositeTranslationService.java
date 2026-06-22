package com.cmx.workermanagemnt.cmx.service.translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.springframework.util.StringUtils;

import com.cmx.workermanagemnt.cmx.config.TranslationProperties;
import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.domain.WorkerSkills;
import com.cmx.workermanagemnt.cmx.exception.TranslationException;
import com.cmx.workermanagemnt.cmx.service.TranslationService;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerBasicInfoDto;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRegistrationRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerResponse;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerSkillsDto;

public class CompositeTranslationService implements TranslationService {

	private final PropertyFileTranslationService propertyFileTranslationService;
	private final BatchTranslationClient cloudTranslationClient;
	private final TranslationProperties properties;

	public CompositeTranslationService(PropertyFileTranslationService propertyFileTranslationService,
			BatchTranslationClient cloudTranslationClient, TranslationProperties properties) {
		this.propertyFileTranslationService = propertyFileTranslationService;
		this.cloudTranslationClient = cloudTranslationClient;
		this.properties = properties;
	}

	@Override
	public WorkerRegistrationRequest toEnglish(WorkerRegistrationRequest request, Locale sourceLocale) {
		if (TranslatableWorkerFields.isEnglish(sourceLocale)) {
			return request;
		}
		String language = sourceLocale.getLanguage();
		WorkerRegistrationRequest copy = TranslatableWorkerFields.copyRequest(request);
		TranslationBatch batch = new TranslationBatch();

		WorkerBasicInfoDto basic = copy.getBasicInfo();
		batch.addToEnglish(basic.getCity(), language, basic::setCity, propertyFileTranslationService);
		batch.addToEnglish(basic.getState(), language, basic::setState, propertyFileTranslationService);
		batch.addToEnglish(basic.getAddress(), language, basic::setAddress, propertyFileTranslationService);

		WorkerSkillsDto skills = copy.getSkills();
		batch.addToEnglish(skills.getPrimarySkill(), language, skills::setPrimarySkill, propertyFileTranslationService);
		batch.addListToEnglish(skills.getSecondarySkills(), language, skills::setSecondarySkills, propertyFileTranslationService);
		batch.addListToEnglish(skills.getCertifications(), language, skills::setCertifications, propertyFileTranslationService);
		batch.addListToEnglish(skills.getToolsOwned(), language, skills::setToolsOwned, propertyFileTranslationService);

		flushBatch(batch, language, "en", properties.getOnFailure().shouldFailOnWrite());
		return copy;
	}

	@Override
	public WorkerResponse fromEnglish(WorkerBasicInfo basicInfo, WorkerSkills skills, Locale targetLocale) {
		if (TranslatableWorkerFields.isEnglish(targetLocale)) {
			return WorkerResponse.fromDomain(basicInfo, skills);
		}
		String language = targetLocale.getLanguage();
		WorkerBasicInfo localizedBasic = TranslatableWorkerFields.copyBasicInfo(basicInfo);
		WorkerSkills localizedSkills = TranslatableWorkerFields.copySkills(skills);
		TranslationBatch batch = new TranslationBatch();

		batch.addFromEnglish(basicInfo.getCity(), language, localizedBasic::setCity, propertyFileTranslationService);
		batch.addFromEnglish(basicInfo.getState(), language, localizedBasic::setState, propertyFileTranslationService);
		batch.addFromEnglish(basicInfo.getAddress(), language, localizedBasic::setAddress, propertyFileTranslationService);
		batch.addFromEnglish(skills.getPrimarySkill(), language, localizedSkills::setPrimarySkill, propertyFileTranslationService);
		batch.addListFromEnglish(skills.getSecondarySkills(), language, localizedSkills::setSecondarySkills, propertyFileTranslationService);
		batch.addListFromEnglish(skills.getCertifications(), language, localizedSkills::setCertifications, propertyFileTranslationService);
		batch.addListFromEnglish(skills.getToolsOwned(), language, localizedSkills::setToolsOwned, propertyFileTranslationService);

		try {
			flushBatch(batch, "en", language, properties.getOnFailure().shouldFailOnRead());
		}
		catch (TranslationException ex) {
			if (properties.getOnFailure().shouldFailOnRead()) {
				throw ex;
			}
			return WorkerResponse.fromDomain(basicInfo, skills);
		}
		return WorkerResponse.fromDomain(localizedBasic, localizedSkills);
	}

	private void flushBatch(TranslationBatch batch, String sourceLanguage, String targetLanguage, boolean failOnError) {
		if (batch.cloudTexts.isEmpty()) {
			return;
		}
		try {
			List<String> translated = cloudTranslationClient.translateBatch(batch.cloudTexts, sourceLanguage, targetLanguage);
			for (int i = 0; i < batch.appliers.size(); i++) {
				batch.appliers.get(i).accept(translated.get(i));
			}
		}
		catch (TranslationException ex) {
			if (failOnError) {
				throw ex;
			}
		}
	}

	private static final class TranslationBatch {

		private final List<String> cloudTexts = new ArrayList<>();
		private final List<Consumer<String>> appliers = new ArrayList<>();

		private void addToEnglish(String value, String language, Consumer<String> setter,
				PropertyFileTranslationService propertyFile) {
			if (!StringUtils.hasText(value)) {
				return;
			}
			if (propertyFile.hasToEnglishMapping(value, language)) {
				setter.accept(propertyFile.toEnglishValue(value, language));
				return;
			}
			cloudTexts.add(value);
			appliers.add(setter);
		}

		private void addFromEnglish(String value, String language, Consumer<String> setter,
				PropertyFileTranslationService propertyFile) {
			if (!StringUtils.hasText(value)) {
				return;
			}
			if (propertyFile.hasFromEnglishMapping(value, language)) {
				setter.accept(propertyFile.fromEnglishValue(value, language));
				return;
			}
			cloudTexts.add(value);
			appliers.add(setter);
		}

		private void addListToEnglish(List<String> values, String language, Consumer<List<String>> setter,
				PropertyFileTranslationService propertyFile) {
			if (values == null || values.isEmpty()) {
				return;
			}
			List<String> resolved = new ArrayList<>(values.size());
			for (String value : values) {
				if (!StringUtils.hasText(value)) {
					resolved.add(value);
					continue;
				}
				if (propertyFile.hasToEnglishMapping(value, language)) {
					resolved.add(propertyFile.toEnglishValue(value, language));
					continue;
				}
				int resolvedIndex = resolved.size();
				resolved.add(null);
				cloudTexts.add(value);
				appliers.add(translated -> resolved.set(resolvedIndex, translated));
			}
			setter.accept(resolved);
		}

		private void addListFromEnglish(List<String> values, String language, Consumer<List<String>> setter,
				PropertyFileTranslationService propertyFile) {
			if (values == null || values.isEmpty()) {
				return;
			}
			List<String> resolved = new ArrayList<>(values.size());
			for (String value : values) {
				if (!StringUtils.hasText(value)) {
					resolved.add(value);
					continue;
				}
				if (propertyFile.hasFromEnglishMapping(value, language)) {
					resolved.add(propertyFile.fromEnglishValue(value, language));
					continue;
				}
				int resolvedIndex = resolved.size();
				resolved.add(null);
				cloudTexts.add(value);
				appliers.add(translated -> resolved.set(resolvedIndex, translated));
			}
			setter.accept(resolved);
		}
	}
}
