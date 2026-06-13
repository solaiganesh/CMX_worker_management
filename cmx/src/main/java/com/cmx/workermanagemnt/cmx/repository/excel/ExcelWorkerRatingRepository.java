package com.cmx.workermanagemnt.cmx.repository.excel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import com.cmx.workermanagemnt.cmx.domain.WorkerRating;
import com.cmx.workermanagemnt.cmx.repository.WorkerRatingRepository;

@Repository
@ConditionalOnProperty(name = "cmx.storage.type", havingValue = "excel")
public class ExcelWorkerRatingRepository implements WorkerRatingRepository {

	private final ExcelFileSupport excelFileSupport;

	public ExcelWorkerRatingRepository(ExcelFileSupport excelFileSupport) {
		this.excelFileSupport = excelFileSupport;
	}

	@Override
	public WorkerRating save(WorkerRating rating) {
		excelFileSupport.withWorkbook(excelFileSupport.getRatingsPath(), workbook -> {
			Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : workbook.createSheet("data");
			Row row = sheet.createRow(ExcelFileSupport.appendRow(sheet));
			writeRow(row, rating);
		});
		return rating;
	}

	@Override
	public List<WorkerRating> findByWorkerId(String workerId) {
		return excelFileSupport.readWorkbook(excelFileSupport.getRatingsPath(), workbook -> {
			if (workbook == null || workbook.getNumberOfSheets() == 0) {
				return List.of();
			}
			Sheet sheet = workbook.getSheetAt(0);
			List<WorkerRating> ratings = new ArrayList<>();
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				WorkerRating rating = readRow(row);
				if (workerId.equals(rating.getWorkerId())) {
					ratings.add(rating);
				}
			}
			return ratings;
		});
	}

	@Override
	public double calculateAverageRating(String workerId) {
		List<WorkerRating> ratings = findByWorkerId(workerId);
		if (ratings.isEmpty()) {
			return 0.0;
		}
		return ratings.stream()
				.mapToInt(WorkerRating::getScore)
				.average()
				.orElse(0.0);
	}

	@Override
	public int countByWorkerId(String workerId) {
		return findByWorkerId(workerId).size();
	}

	private WorkerRating readRow(Row row) {
		WorkerRating rating = new WorkerRating();
		rating.setId(ExcelFileSupport.cellString(row.getCell(0)));
		rating.setWorkerId(ExcelFileSupport.cellString(row.getCell(1)));
		String score = ExcelFileSupport.cellString(row.getCell(2));
		if (!score.isBlank()) {
			rating.setScore(Integer.parseInt(score));
		}
		rating.setReviewerName(ExcelFileSupport.cellString(row.getCell(3)));
		rating.setComment(ExcelFileSupport.cellString(row.getCell(4)));
		rating.setCreatedAt(ExcelFileSupport.parseInstant(ExcelFileSupport.cellString(row.getCell(5))));
		return rating;
	}

	private void writeRow(Row row, WorkerRating rating) {
		ExcelFileSupport.setCell(row, 0, rating.getId());
		ExcelFileSupport.setCell(row, 1, rating.getWorkerId());
		ExcelFileSupport.setCell(row, 2, Integer.toString(rating.getScore()));
		ExcelFileSupport.setCell(row, 3, rating.getReviewerName());
		ExcelFileSupport.setCell(row, 4, rating.getComment());
		ExcelFileSupport.setCell(row, 5, rating.getCreatedAt() != null ? rating.getCreatedAt().toString() : Instant.now().toString());
	}
}
