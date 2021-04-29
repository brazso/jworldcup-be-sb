package com.zematix.jworldcup.backend.dto;

import java.util.Objects;
import java.util.Set;

public class GenericSetResponse<T> extends CommonResponse{

    private Set<T> data;
    
    public GenericSetResponse(Set<T> data) {
        super();
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        @SuppressWarnings("unchecked")
        Set<T> that = (Set<T>) o;
        return data.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }


    public Set<T> getData() {
        return data;
    }

    public void setData(Set<T> data) {
        this.data = data;
    }
}
