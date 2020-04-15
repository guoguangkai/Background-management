package com.yingxue.lesson.config;

import com.yingxue.lesson.exception.code.BaseResponseCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.*;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger2 配置类
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Value("${swagger2.enable}")
    private boolean enable;
    @Bean
    public Docket createRestApi() {
        /*List<Parameter> pars = new ArrayList<>();
        ParameterBuilder tokenPar = new ParameterBuilder();
        //业务token
        tokenPar.name("authorization").description("swagger测试用(模拟authorization传入)非必填 header").modelRef(new ModelRef("string")).parameterType("header").required(false);
        pars.add(tokenPar.build());*/

        //设置全局响应状态码
        List<ResponseMessage> responseMessageList = new ArrayList<>();
        responseMessageList.add(new ResponseMessageBuilder().code(404).message("找不到资源").responseModel(new ModelRef("ApiError")).build());
        responseMessageList.add(new ResponseMessageBuilder().code(400).message("参数错误").responseModel(new ModelRef("ApiError")).build());
        responseMessageList.add(new ResponseMessageBuilder().code(401).message("没有认证").responseModel(new ModelRef("ApiError")).build());
        responseMessageList.add(new ResponseMessageBuilder().code(500).message("服务器内部错误").responseModel(new ModelRef("ApiError")).build());
        responseMessageList.add(new ResponseMessageBuilder().code(403).message("没有没有访问权限").responseModel(new ModelRef("ApiError")).build());
        responseMessageList.add(new ResponseMessageBuilder().code(200).message("请求成功").responseModel(new ModelRef("ApiError")).build());


        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.POST, customerResponseMessage())
                .globalResponseMessage(RequestMethod.GET, customerResponseMessage())
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.zgx.tracking_center.controller"))
                .paths(PathSelectors.any())
                .build()
                //.globalOperationParameters(pars)
                .enable(enable);

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("中港星-埋点数据管理平台")
                .description("内部用")
                .termsOfServiceUrl("")
                .version("1.0")
                .build();
    }

    private List<ResponseMessage> customerResponseMessage() {
        List<ResponseMessage> list = new ArrayList<>();
        list.add(new ResponseMessageBuilder().code(BaseResponseCode.SUCCESS.getCode()).message(BaseResponseCode.SUCCESS.getMsg()).build());
        list.add(new ResponseMessageBuilder().code(BaseResponseCode.DATA_ERROR.getCode()).message(BaseResponseCode.DATA_ERROR.getMsg()).build());
        list.add(new ResponseMessageBuilder().code(BaseResponseCode.DATA_ERROR.getCode()).message(BaseResponseCode.DATA_ERROR.getMsg()).build());
        list.add(new ResponseMessageBuilder().code(BaseResponseCode.SYSTEM_ERROR.getCode()).message(BaseResponseCode.SYSTEM_ERROR.getMsg()).build());
        return list;
    }
}
