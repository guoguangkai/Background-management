package com.yingxue.lesson.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @NotEmpty 用在集合类上面
 * @NotBlank 用在String上面
 * @NotNull 用在基本类型上
 * @Valid:启用校验
 */
@Data
public class LoginRespVO {
    @ApiModelProperty(value = "正常的业务token")
    private String accessToken;
    @ApiModelProperty(value = "刷新token")
    private String refreshToken;
    @ApiModelProperty(value = "用户id")
    private String id;
    @ApiModelProperty(value = "手机号")
    private String phone;
    @ApiModelProperty(value = "账号")
    private String username;
}
