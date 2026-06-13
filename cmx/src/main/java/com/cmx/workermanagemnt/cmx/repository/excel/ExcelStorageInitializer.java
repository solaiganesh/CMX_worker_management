package com.cmx.workermanagemnt.cmx.repository.excel;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "cmx.storage.type", havingValue = "excel")
public class ExcelStorageInitializer {

	private final ExcelFileSupport excelFileSupport;

	public ExcelStorageInitializer(ExcelFileSupport excelFileSupport) {
		this.excelFileSupport = excelFileSupport;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initialize() {
		excelFileSupport.initializeWorkbooks();
	}
}
