package com.zematix.jworldcup.backend.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Simple generic pair class. 
 */
@Data @RequiredArgsConstructor @AllArgsConstructor
public class Pair<T> {
	private T value1;
	private T value2;
	
	public T getValueN(int n) {
		checkArgument(n==0 || n==1, "Argument \"n\" must be 0 or 1");
		return n==0 ? value1 : value2;
	}

	public void setValueN(int n, T value) {
		checkArgument(n==0 || n==1, "Argument \"n\" must be 0 or 1");
		if (n==0) {
			value1 = value;
		}
		else {
			value2 = value;
		}
	}
	
	/**
	 * Returns {@code true} if both values are {@code null}.
	 * 
	 * @return {@code true} if both values are {@code null}
	 */
	public boolean isEmpty() {
		return value1 == null && value2 == null; 
	}

	/**
	 * Returns the number of non {@code null} values.
	 * 
	 * @return number of non-null values
	 */
	public int size() {
		return (value1 != null ? 1 : 0) + (value2 != null ? 1 : 0); 
	}

	/**
	 * Returns pair values in a list
	 * @return pair values in list
	 */
	public List<T> getList() {
		return Arrays.asList(value1, value2);
	}
}
