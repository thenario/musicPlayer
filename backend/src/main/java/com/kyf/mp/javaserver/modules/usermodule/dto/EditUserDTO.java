package com.kyf.mp.javaserver.modules.usermodule.dto;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class EditUserDTO {
    private String user_name;
    private MultipartFile user_cover;
}
