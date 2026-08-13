package com.kyf.mp.server.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 *
 * </p>
 *
 * @author kyf
 * @since 2026-04-05
 */
@Getter
@Setter
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private Long userId;

    @JsonProperty("user_name")
    @NotBlank(message = "用户名不能为空")
    private String userName;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("user_cover_url")
    private String userCoverUrl;

    @NotBlank(message = "密码不能为空")
    private String password;
}
