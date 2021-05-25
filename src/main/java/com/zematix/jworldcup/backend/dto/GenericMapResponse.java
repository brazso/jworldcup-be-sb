package com.zematix.jworldcup.backend.dto;

import java.util.Map;
import java.util.Objects;

public class GenericMapResponse<K, V> extends CommonResponse{

    private Map<K, V> data;
    
    public GenericMapResponse(Map<K, V> data) {
        super();
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        @SuppressWarnings("unchecked")
        Map<K, V> that = (Map<K, V>) o;
        return data.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }


    public Map<K, V> getData() {
        return data;
    }

    public void setData(Map<K, V> data) {
        this.data = data;
    }
}
