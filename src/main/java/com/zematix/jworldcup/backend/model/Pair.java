package com.zematix.jworldcup.backend.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.List;

/**
 * Simple generic pair class. 
 */
public class Pair<T> {
	private T value1;
	private T value2;
	
	public Pair() {
	}

	public Pair(T value1, T value2) {
		this.value1 = value1;
		this.value2 = value2;
	}

	public T getValue1() {
		return value1;
	}
	public void setValue1(T value1) {
		this.value1 = value1;
	}
	public T getValue2() {
		return value2;
	}
	public void setValue2(T value2) {
		this.value2 = value2;
	}
	
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value1 == null) ? 0 : value1.hashCode());
		result = prime * result + ((value2 == null) ? 0 : value2.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<T> other = (Pair<T>) obj;
		if (value1 == null) {
			if (other.value1 != null)
				return false;
		} else if (!value1.equals(other.value1))
			return false;
		if (value2 == null) {
			if (other.value2 != null)
				return false;
		} else if (!value2.equals(other.value2))
			return false;
		return true;
	}
}
