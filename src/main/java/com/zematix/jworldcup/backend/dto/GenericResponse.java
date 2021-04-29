package com.zematix.jworldcup.backend.dto;

import java.util.Objects;

public class GenericResponse<T> extends CommonResponse{

    private T data;
    
    public GenericResponse(T data) {
        super();
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        @SuppressWarnings("unchecked")
        T that = (T) o;
        return data.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
