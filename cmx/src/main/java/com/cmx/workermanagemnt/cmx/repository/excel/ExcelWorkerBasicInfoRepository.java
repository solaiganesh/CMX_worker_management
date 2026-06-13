package com.cmx.workermanagemnt.cmx.repository.excel;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import com.cmx.workermanagemnt.cmx.domain.AvailabilityStatus;
import com.cmx.workermanagemnt.cmx.domain.Gender;
import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.repository.WorkerBasicInfoRepository;

@Repository
@ConditionalOnProperty(name = "cmx.storage.type", havingValue = "excel")
public class ExcelWorkerBasicInfoRepository implements WorkerBasicInfoRepository {

	private final ExcelFileSupport excelFileSupport;

	public ExcelWorkerBasicInfoRepository(ExcelFileSupport excelFileSupport) {
		this.excelFileSupport = excelFileSupport;
	}

	@Override
	public WorkerBasicInfo save(WorkerBasicInfo info) {
		excelFileSupport.withWorkbook(excelFileSupport.getBasicInfoPath(), workbook -> {
			Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : workbook.createSheet("data");
			int existingRowIndex = findRowIndex(sheet, info.getId());
			Row row = existingRowIndex >= 0 ? sheet.getRow(existingRowIndex) : sheet.createRow(ExcelFileSupport.appendRow(sheet));
			writeRow(row, info);
		});
		return info;
	}

	@Override
	public Optional<WorkerBasicInfo> findById(String id) {
		return excelFileSupport.readWorkbook(excelFileSupport.getBasicInfoPath(), workbook -> {
			if (workbook == null || workbook.getNumberOfSheets() == 0) {
				return Optional.empty();
			}
			Sheet sheet = workbook.getSheetAt(0);
			int rowIndex = findRowIndex(sheet, id);
			if (rowIndex < 0) {
				return Optional.empty();
			}
			return Optional.of(readRow(sheet.getRow(rowIndex)));
		});
	}

	@Override
	public List<WorkerBasicInfo> findAll() {
		return excelFileSupport.readWorkbook(excelFileSupport.getBasicInfoPath(), workbook -> {
			if (workbook == null || workbook.getNumberOfSheets() == 0) {
				return List.of();
			}
			Sheet sheet = workbook.getSheetAt(0);
			List<WorkerBasicInfo> workers = new ArrayList<>();
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				workers.add(readRow(row));
			}
			return workers;
		});
	}

	@Override
	public Optional<WorkerBasicInfo> findByMobileNumber(String mobileNumber) {
		String normalized = normalizeMobile(mobileNumber);
		return excelFileSupport.readWorkbook(excelFileSupport.getBasicInfoPath(), workbook -> {
			if (workbook == null || workbook.getNumberOfSheets() == 0) {
				return Optional.empty();
			}
			Sheet sheet = workbook.getSheetAt(0);
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				String existingMobile = ExcelFileSupport.cellString(row.getCell(2));
				if (normalized.equals(normalizeMobile(existingMobile))) {
					return Optional.of(readRow(row));
				}
			}
			return Optional.empty();
		});
	}

	@Override
	public List<WorkerBasicInfo> findByFullNameIgnoreCase(String fullName) {
		if (fullName == null || fullName.isBlank()) {
			return List.of();
		}
		String target = fullName.trim();
		return excelFileSupport.readWorkbook(excelFileSupport.getBasicInfoPath(), workbook -> {
			if (workbook == null || workbook.getNumberOfSheets() == 0) {
				return List.of();
			}
			Sheet sheet = workbook.getSheetAt(0);
			List<WorkerBasicInfo> matches = new ArrayList<>();
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				String existingName = ExcelFileSupport.cellString(row.getCell(1));
				if (target.equalsIgnoreCase(existingName)) {
					matches.add(readRow(row));
				}
			}
			return matches;
		});
	}

	@Override
	public boolean existsByMobileNumber(String mobileNumber) {
		return findByMobileNumber(mobileNumber).isPresent();
	}

	@Override
	public boolean existsByEmail(String email) {
		return excelFileSupport.readWorkbook(excelFileSupport.getBasicInfoPath(), workbook -> {
			if (workbook == null || workbook.getNumberOfSheets() == 0) {
				return false;
			}
			Sheet sheet = workbook.getSheetAt(0);
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				String existingEmail = ExcelFileSupport.cellString(row.getCell(3));
				if (email.equalsIgnoreCase(existingEmail)) {
					return true;
				}
			}
			return false;
		});
	}

	@Override
	public void deleteById(String id) {
		excelFileSupport.withWorkbook(excelFileSupport.getBasicInfoPath(), workbook -> {
			if (workbook.getNumberOfSheets() == 0) {
				return;
			}
			Sheet sheet = workbook.getSheetAt(0);
			int rowIndex = findRowIndex(sheet, id);
			if (rowIndex >= 0) {
				Row row = sheet.getRow(rowIndex);
				if (row != null) {
					sheet.removeRow(row);
				}
			}
		});
	}

	private int findRowIndex(Sheet sheet, String id) {
		if (id == null || sheet.getLastRowNum() < 1) {
			return -1;
		}
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			if (id.equals(ExcelFileSupport.cellString(row.getCell(0)))) {
				return i;
			}
		}
		return -1;
	}

	private WorkerBasicInfo readRow(Row row) {
		WorkerBasicInfo info = new WorkerBasicInfo();
		info.setId(ExcelFileSupport.cellString(row.getCell(0)));
		info.setFullName(ExcelFileSupport.cellString(row.getCell(1)));
		info.setMobileNumber(ExcelFileSupport.cellString(row.getCell(2)));
		info.setEmail(ExcelFileSupport.cellString(row.getCell(3)));
		info.setProfilePhotoUrl(ExcelFileSupport.cellString(row.getCell(4)));
		info.setDateOfBirth(ExcelFileSupport.parseDate(ExcelFileSupport.cellString(row.getCell(5))));
		String gender = ExcelFileSupport.cellString(row.getCell(6));
		if (!gender.isBlank()) {
			info.setGender(Gender.valueOf(gender));
		}
		info.setCity(ExcelFileSupport.cellString(row.getCell(7)));
		info.setState(ExcelFileSupport.cellString(row.getCell(8)));
		info.setAddress(ExcelFileSupport.cellString(row.getCell(9)));
		info.setPincode(ExcelFileSupport.cellString(row.getCell(10)));
		info.setLatitude(ExcelFileSupport.parseDecimal(ExcelFileSupport.cellString(row.getCell(11))));
		info.setLongitude(ExcelFileSupport.parseDecimal(ExcelFileSupport.cellString(row.getCell(12))));
		info.setPrimaryLanguage(ExcelFileSupport.cellString(row.getCell(13)));
		String availability = ExcelFileSupport.cellString(row.getCell(14));
		if (!availability.isBlank()) {
			info.setAvailabilityStatus(AvailabilityStatus.valueOf(availability));
		}
		info.setCreatedAt(ExcelFileSupport.parseInstant(ExcelFileSupport.cellString(row.getCell(15))));
		info.setUpdatedAt(ExcelFileSupport.parseInstant(ExcelFileSupport.cellString(row.getCell(16))));
		return info;
	}

	private void writeRow(Row row, WorkerBasicInfo info) {
		ExcelFileSupport.setCell(row, 0, info.getId());
		ExcelFileSupport.setCell(row, 1, info.getFullName());
		ExcelFileSupport.setCell(row, 2, info.getMobileNumber());
		ExcelFileSupport.setCell(row, 3, info.getEmail());
		ExcelFileSupport.setCell(row, 4, info.getProfilePhotoUrl());
		ExcelFileSupport.setCell(row, 5, info.getDateOfBirth() != null ? info.getDateOfBirth().toString() : "");
		ExcelFileSupport.setCell(row, 6, info.getGender() != null ? info.getGender().name() : "");
		ExcelFileSupport.setCell(row, 7, info.getCity());
		ExcelFileSupport.setCell(row, 8, info.getState());
		ExcelFileSupport.setCell(row, 9, info.getAddress());
		ExcelFileSupport.setCell(row, 10, info.getPincode());
		ExcelFileSupport.setCell(row, 11, info.getLatitude() != null ? info.getLatitude().toPlainString() : "");
		ExcelFileSupport.setCell(row, 12, info.getLongitude() != null ? info.getLongitude().toPlainString() : "");
		ExcelFileSupport.setCell(row, 13, info.getPrimaryLanguage());
		ExcelFileSupport.setCell(row, 14, info.getAvailabilityStatus() != null ? info.getAvailabilityStatus().name() : "");
		ExcelFileSupport.setCell(row, 15, info.getCreatedAt() != null ? info.getCreatedAt().toString() : Instant.now().toString());
		ExcelFileSupport.setCell(row, 16, info.getUpdatedAt() != null ? info.getUpdatedAt().toString() : Instant.now().toString());
	}

	private static String normalizeMobile(String mobileNumber) {
		if (mobileNumber == null) {
			return "";
		}
		return mobileNumber.replaceAll("\\s+", "").trim();
	}
}
