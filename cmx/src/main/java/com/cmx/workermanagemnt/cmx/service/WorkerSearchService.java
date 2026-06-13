package com.cmx.workermanagemnt.cmx.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.domain.WorkerSkills;
import com.cmx.workermanagemnt.cmx.repository.WorkerBasicInfoRepository;
import com.cmx.workermanagemnt.cmx.repository.WorkerRatingRepository;
import com.cmx.workermanagemnt.cmx.repository.WorkerSkillsRepository;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerSearchRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerSearchResponse;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerSearchResult;

@Service
public class WorkerSearchService {

	private final WorkerBasicInfoRepository basicInfoRepository;
	private final WorkerSkillsRepository skillsRepository;
	private final WorkerRatingRepository ratingRepository;
	private final CityTranslationSupport cityTranslationSupport;

	public WorkerSearchService(WorkerBasicInfoRepository basicInfoRepository, WorkerSkillsRepository skillsRepository,
			WorkerRatingRepository ratingRepository, CityTranslationSupport cityTranslationSupport) {
		this.basicInfoRepository = basicInfoRepository;
		this.skillsRepository = skillsRepository;
		this.ratingRepository = ratingRepository;
		this.cityTranslationSupport = cityTranslationSupport;
	}

	public WorkerSearchResponse search(WorkerSearchRequest request, Locale inputLocale, Locale responseLocale) {
		String searchCity = request.getCity();
		if (searchCity != null) {
			searchCity = cityTranslationSupport.toEnglish(searchCity, inputLocale);
		}

		Map<String, WorkerSkills> skillsByUserId = skillsRepository.findAll().stream()
				.collect(Collectors.toMap(WorkerSkills::getUserId, skills -> skills, (left, right) -> left));

		List<WorkerSearchResult> filtered = new ArrayList<>();
		for (WorkerBasicInfo basicInfo : basicInfoRepository.findAll()) {
			WorkerSkills skills = skillsByUserId.get(basicInfo.getId());
			if (!matchesFilters(basicInfo, skills, request, searchCity)) {
				continue;
			}
			filtered.add(toSearchResult(basicInfo, skills, responseLocale));
		}

		int page = request.getPage() != null ? request.getPage() : 0;
		int size = request.getSize() != null ? request.getSize() : 20;
		int fromIndex = Math.min(page * size, filtered.size());
		int toIndex = Math.min(fromIndex + size, filtered.size());

		WorkerSearchResponse response = new WorkerSearchResponse();
		response.setItems(filtered.subList(fromIndex, toIndex));
		response.setTotal(filtered.size());
		response.setPage(page);
		response.setSize(size);
		return response;
	}

	private boolean matchesFilters(WorkerBasicInfo basicInfo, WorkerSkills skills, WorkerSearchRequest request,
			String searchCity) {
		if (searchCity != null && !searchCity.equalsIgnoreCase(basicInfo.getCity())) {
			return false;
		}
		if (request.getMinAge() != null || request.getMaxAge() != null) {
			if (basicInfo.getDateOfBirth() == null) {
				return false;
			}
			int age = Period.between(basicInfo.getDateOfBirth(), LocalDate.now()).getYears();
			if (request.getMinAge() != null && age < request.getMinAge()) {
				return false;
			}
			if (request.getMaxAge() != null && age > request.getMaxAge()) {
				return false;
			}
		}
		if (request.getMinExperience() != null || request.getMaxExperience() != null) {
			if (skills == null || skills.getExperienceYears() == null) {
				return false;
			}
			if (request.getMinExperience() != null
					&& skills.getExperienceYears().compareTo(request.getMinExperience()) < 0) {
				return false;
			}
			if (request.getMaxExperience() != null
					&& skills.getExperienceYears().compareTo(request.getMaxExperience()) > 0) {
				return false;
			}
		}
		if (request.getMinRating() != null) {
			double average = WorkerRatingService.roundAverage(ratingRepository.calculateAverageRating(basicInfo.getId()));
			if (BigDecimal.valueOf(average).compareTo(request.getMinRating()) < 0) {
				return false;
			}
		}
		return true;
	}

	private WorkerSearchResult toSearchResult(WorkerBasicInfo basicInfo, WorkerSkills skills, Locale responseLocale) {
		WorkerSearchResult result = new WorkerSearchResult();
		result.setId(basicInfo.getId());
		result.setFullName(basicInfo.getFullName());
		result.setEmail(basicInfo.getEmail());
		result.setMobileNumber(basicInfo.getMobileNumber());
		result.setCity(cityTranslationSupport.fromEnglish(basicInfo.getCity(), responseLocale));
		result.setExperienceYears(skills != null ? skills.getExperienceYears() : null);
		result.setAverageRating(WorkerRatingService.roundAverage(ratingRepository.calculateAverageRating(basicInfo.getId())));
		result.setRatingCount(ratingRepository.countByWorkerId(basicInfo.getId()));
		return result;
	}
}
