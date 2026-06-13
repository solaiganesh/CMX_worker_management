package com.cmx.workermanagemnt.cmx.web.dto;

import java.util.ArrayList;
import java.util.List;

public class WorkerSearchResponse {

	private List<WorkerSearchResult> items = new ArrayList<>();
	private long total;
	private int page;
	private int size;

	public List<WorkerSearchResult> getItems() {
		return items;
	}

	public void setItems(List<WorkerSearchResult> items) {
		this.items = items != null ? items : new ArrayList<>();
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
