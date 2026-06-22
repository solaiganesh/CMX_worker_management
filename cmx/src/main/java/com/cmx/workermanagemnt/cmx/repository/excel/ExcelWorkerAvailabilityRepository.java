package com.cmx.workermanagemnt.cmx.repository.excel;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import com.cmx.workermanagemnt.cmx.domain.WorkerAvailability;
import com.cmx.workermanagemnt.cmx.repository.WorkerAvailabilityRepository;

@Repository
@ConditionalOnProperty(name = "cmx.storage.type", havingValue = "excel")
public class ExcelWorkerAvailabilityRepository implements WorkerAvailabilityRepository {

	private final ExcelFileSupport excelFileSupport;

	public ExcelWorkerAvailabilityRepository(ExcelFileSupport excelFileSupport) {
		this.excelFileSupport = excelFileSupport;
	}

	@Override
	public WorkerAvailability save(WorkerAvailability availability) {
		excelFileSupport.withWorkbook(excelFileSupport.getAvailabilityPath(), workbook -> {
			Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : workbook.createSheet("data");
			int existingRowIndex = findRowIndex(sheet, availability.getWorkerId(), availability.getAvailabilityDate());
			Row row = existingRowIndex >= 0 ? sheet.getRow(existingRowIndex)
					: sheet.createRow(ExcelFileSupport.appendRow(sheet));
			if (existingRowIndex < 0 && availability.getId() == null) {
				availability.setId(java.util.UUID.randomUUID().toString());
			}
			else if (existingRowIndex >= 0 && availability.getId() == null) {
				availability.setId(ExcelFileSupport.cellString(row.getCell(0)));
			}
			writeRow(row, availability);
		});
		return availability;
	}

	@Override
	public List<WorkerAvailability> findByWorkerId(String workerId) {
		return excelFileSupport.readWorkbook(excelFileSupport.getAvailabilityPath(), workbook -> {
			if (workbook == null || workbook.getNumberOfSheets() == 0) {
				return List.of();
			}
			Sheet sheet = workbook.getSheetAt(0);
			List<WorkerAvailability> entries = new ArrayList<>();
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				WorkerAvailability availability = readRow(row);
				if (workerId.equals(availability.getWorkerId())) {
					entries.add(availability);
				}
			}
			return entries;
		});
	}

	@Override
	public Optional<WorkerAvailability> findByWorkerIdAndDate(String workerId, LocalDate date) {
		return excelFileSupport.readWorkbook(excelFileSupport.getAvailabilityPath(), workbook -> {
			if (workbook == null || workbook.getNumberOfSheets() == 0) {
				return Optional.empty();
			}
			Sheet sheet = workbook.getSheetAt(0);
			int rowIndex = findRowIndex(sheet, workerId, date);
			if (rowIndex < 0) {
				return Optional.empty();
			}
			return Optional.of(readRow(sheet.getRow(rowIndex)));
		});
	}

	@Override
	public boolean isAvailableOnDate(String workerId, LocalDate date) {
		return findByWorkerIdAndDate(workerId, date)
				.map(WorkerAvailability::isAvailable)
				.orElse(false);
	}

	private int findRowIndex(Sheet sheet, String workerId, LocalDate date) {
		if (workerId == null || date == null || sheet.getLastRowNum() < 1) {
			return -1;
		}
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			String existingWorkerId = ExcelFileSupport.cellString(row.getCell(1));
			LocalDate existingDate = ExcelFileSupport.parseDate(ExcelFileSupport.cellString(row.getCell(2)));
			if (workerId.equals(existingWorkerId) && date.equals(existingDate)) {
				return i;
			}
		}
		return -1;
	}

	private WorkerAvailability readRow(Row row) {
		WorkerAvailability availability = new WorkerAvailability();
		availability.setId(ExcelFileSupport.cellString(row.getCell(0)));
		availability.setWorkerId(ExcelFileSupport.cellString(row.getCell(1)));
		availability.setAvailabilityDate(ExcelFileSupport.parseDate(ExcelFileSupport.cellString(row.getCell(2))));
		String available = ExcelFileSupport.cellString(row.getCell(3));
		availability.setAvailable(Boolean.parseBoolean(available));
		availability.setUpdatedAt(ExcelFileSupport.parseInstant(ExcelFileSupport.cellString(row.getCell(4))));
		return availability;
	}

	private void writeRow(Row row, WorkerAvailability availability) {
		ExcelFileSupport.setCell(row, 0, availability.getId());
		ExcelFileSupport.setCell(row, 1, availability.getWorkerId());
		ExcelFileSupport.setCell(row, 2,
				availability.getAvailabilityDate() != null ? availability.getAvailabilityDate().toString() : "");
		ExcelFileSupport.setCell(row, 3, Boolean.toString(availability.isAvailable()));
		ExcelFileSupport.setCell(row, 4,
				availability.getUpdatedAt() != null ? availability.getUpdatedAt().toString() : Instant.now().toString());
	}
}
