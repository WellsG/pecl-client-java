package com.example.pelc;

import java.io.Serializable;
import java.util.List;

public class PagedList<T> implements Serializable {

	private static final long serialVersionUID = -985311686257129554L;

	private List<T> results;
	private int count;
	private String next;
	private String previous;

	public List<T> getResults() {
		return results;
	}

	public void setResults(List<T> results) {
		this.results = results;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public String getPrevious() {
		return previous;
	}

	public void setPrevious(String previous) {
		this.previous = previous;
	}

	@Override
	public String toString() {
		return "PagedList [results=" + results + ", count=" + count + ", next="
				+ next + ", previous=" + previous + "]";
	}
}
