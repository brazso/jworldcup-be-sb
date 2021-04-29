package com.zematix.jworldcup.backend.dto;

import java.util.List;
import java.util.Objects;

public class GenericListResponse<T> extends CommonResponse{

    private List<T> data;
    
    public GenericListResponse(List<T> data) {
        super();
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        @SuppressWarnings("unchecked")
        List<T> that = (List<T>) o;
        return data.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }


    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
