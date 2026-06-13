package com.cmx.workermanagemnt.cmx.repository.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import com.cmx.workermanagemnt.cmx.domain.SkillLevel;
import com.cmx.workermanagemnt.cmx.domain.WorkType;
import com.cmx.workermanagemnt.cmx.domain.WorkerSkills;
import com.cmx.workermanagemnt.cmx.repository.WorkerSkillsRepository;

@Repository
@ConditionalOnProperty(name = "cmx.storage.type", havingValue = "excel")
public class ExcelWorkerSkillsRepository implements WorkerSkillsRepository {

	private final ExcelFileSupport excelFileSupport;

	public ExcelWorkerSkillsRepository(ExcelFileSupport excelFileSupport) {
		this.excelFileSupport = excelFileSupport;
	}

	@Override
	public WorkerSkills save(WorkerSkills skills) {
		excelFileSupport.withWorkbook(excelFileSupport.getSkillsPath(), workbook -> {
			Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : workbook.createSheet("data");
			int existingRowIndex = findRowIndex(sheet, skills.getUserId());
			Row row = existingRowIndex >= 0 ? sheet.getRow(existingRowIndex) : sheet.createRow(ExcelFileSupport.appendRow(sheet));
			writeRow(row, skills);
		});
		return skills;
	}

	@Override
	public Optional<WorkerSkills> findByUserId(String userId) {
		return excelFileSupport.readWorkbook(excelFileSupport.getSkillsPath(), workbook -> {
			if (workbook == null || workbook.getNumberOfSheets() == 0) {
				return Optional.empty();
			}
			Sheet sheet = workbook.getSheetAt(0);
			int rowIndex = findRowIndex(sheet, userId);
			if (rowIndex < 0) {
				return Optional.empty();
			}
			return Optional.of(readRow(sheet.getRow(rowIndex)));
		});
	}

	@Override
	public List<WorkerSkills> findAll() {
		return excelFileSupport.readWorkbook(excelFileSupport.getSkillsPath(), workbook -> {
			if (workbook == null || workbook.getNumberOfSheets() == 0) {
				return List.of();
			}
			Sheet sheet = workbook.getSheetAt(0);
			List<WorkerSkills> skillsList = new ArrayList<>();
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				skillsList.add(readRow(row));
			}
			return skillsList;
		});
	}

	@Override
	public void deleteByUserId(String userId) {
		excelFileSupport.withWorkbook(excelFileSupport.getSkillsPath(), workbook -> {
			if (workbook.getNumberOfSheets() == 0) {
				return;
			}
			Sheet sheet = workbook.getSheetAt(0);
			int rowIndex = findRowIndex(sheet, userId);
			if (rowIndex >= 0) {
				Row row = sheet.getRow(rowIndex);
				if (row != null) {
					sheet.removeRow(row);
				}
			}
		});
	}

	private int findRowIndex(Sheet sheet, String userId) {
		if (userId == null || sheet.getLastRowNum() < 1) {
			return -1;
		}
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			if (userId.equals(ExcelFileSupport.cellString(row.getCell(0)))) {
				return i;
			}
		}
		return -1;
	}

	private WorkerSkills readRow(Row row) {
		WorkerSkills skills = new WorkerSkills();
		skills.setUserId(ExcelFileSupport.cellString(row.getCell(0)));
		skills.setPrimarySkill(ExcelFileSupport.cellString(row.getCell(1)));
		skills.setSecondarySkills(ExcelFileSupport.splitList(ExcelFileSupport.cellString(row.getCell(2))));
		skills.setExperienceYears(ExcelFileSupport.parseDecimal(ExcelFileSupport.cellString(row.getCell(3))));
		String skillLevel = ExcelFileSupport.cellString(row.getCell(4));
		if (!skillLevel.isBlank()) {
			skills.setSkillLevel(SkillLevel.valueOf(skillLevel));
		}
		skills.setCertifications(ExcelFileSupport.splitList(ExcelFileSupport.cellString(row.getCell(5))));
		skills.setToolsOwned(ExcelFileSupport.splitList(ExcelFileSupport.cellString(row.getCell(6))));
		String workType = ExcelFileSupport.cellString(row.getCell(7));
		if (!workType.isBlank()) {
			skills.setWorkType(WorkType.valueOf(workType));
		}
		skills.setLanguagesSpoken(ExcelFileSupport.splitList(ExcelFileSupport.cellString(row.getCell(8))));
		skills.setPortfolioImages(ExcelFileSupport.splitList(ExcelFileSupport.cellString(row.getCell(9))));
		return skills;
	}

	private void writeRow(Row row, WorkerSkills skills) {
		ExcelFileSupport.setCell(row, 0, skills.getUserId());
		ExcelFileSupport.setCell(row, 1, skills.getPrimarySkill());
		ExcelFileSupport.setCell(row, 2, ExcelFileSupport.joinList(skills.getSecondarySkills()));
		ExcelFileSupport.setCell(row, 3, skills.getExperienceYears() != null ? skills.getExperienceYears().toPlainString() : "");
		ExcelFileSupport.setCell(row, 4, skills.getSkillLevel() != null ? skills.getSkillLevel().name() : "");
		ExcelFileSupport.setCell(row, 5, ExcelFileSupport.joinList(skills.getCertifications()));
		ExcelFileSupport.setCell(row, 6, ExcelFileSupport.joinList(skills.getToolsOwned()));
		ExcelFileSupport.setCell(row, 7, skills.getWorkType() != null ? skills.getWorkType().name() : "");
		ExcelFileSupport.setCell(row, 8, ExcelFileSupport.joinList(skills.getLanguagesSpoken()));
		ExcelFileSupport.setCell(row, 9, ExcelFileSupport.joinList(skills.getPortfolioImages()));
	}
}
