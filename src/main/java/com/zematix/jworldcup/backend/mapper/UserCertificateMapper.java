package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;

import com.zematix.jworldcup.backend.dto.UserCertificateDto;
import com.zematix.jworldcup.backend.model.UserCertificate;

@Mapper
public interface UserCertificateMapper extends MapperBase<UserCertificateDto, UserCertificate> {

}
