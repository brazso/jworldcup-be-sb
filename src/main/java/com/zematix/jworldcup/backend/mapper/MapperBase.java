package com.zematix.jworldcup.backend.mapper;

import java.util.List;

import org.mapstruct.Mapper;

/**
 * Base mapper interface for <a href="https://mapstruct.org/">MapStruct</a> interfaces.
 * Inherited this interface from yours and adding {@link Mapper} annotation to yours, 
 * you may use the its inside methods in yours at once.
 *
 * @param <D> - DTO generic type
 * @param <E> - Entity generic type
 */
public interface MapperBase<D, E> {

    E dtoToEntity(D dto);

    D entityToDto(E entity);

    List<E> dtoListToEntityList(List<D> dtoList);

    List<D> entityListToDtoList(List<E> entityList);

}
