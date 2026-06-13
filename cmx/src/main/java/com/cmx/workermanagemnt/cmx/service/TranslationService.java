package com.cmx.workermanagemnt.cmx.service;

import java.util.Locale;

import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.domain.WorkerSkills;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRegistrationRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerResponse;

public interface TranslationService {

	WorkerRegistrationRequest toEnglish(WorkerRegistrationRequest request, Locale sourceLocale);

	WorkerResponse fromEnglish(WorkerBasicInfo basicInfo, WorkerSkills skills, Locale targetLocale);
}
