package com.cdutetc.nanpo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ValidationExceptionTests {
    @Autowired MockMvc mvc;

    @Test
    void validRegistrationSucceeds() throws Exception {
        mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"exp10-valid\",\"password\":\"secret123\","
                        + "\"phone\":\"13800138000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("[exp10-check] valid registration -> code=200 PASS");
    }

    @Test
    void invalidFieldsReturnFieldLevelMessages() throws Exception {
        mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"x\",\"password\":\"secret123\","
                        + "\"phone\":\"12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.msg").value("请求参数格式或取值不正确"))
                .andExpect(jsonPath("$.data.username").value("用户名长度必须在2到20个字符之间"))
                .andExpect(jsonPath("$.data.phone").value("手机号格式不正确"));
        System.out.println("[exp10-check] @Size + @Pattern -> field error map PASS");
    }

    @Test
    void nullUsernameTriggersNotNull() throws Exception {
        mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"secret123\",\"phone\":\"13900139000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.data.username").value("用户名不能为空"));
        System.out.println("[exp10-check] @NotNull username -> field error map PASS");
    }

    @Test
    void duplicatePhoneReturnsBusinessCode() throws Exception {
        String phone = "13700137000";
        mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"exp10-phone-a\",\"password\":\"secret123\","
                        + "\"phone\":\"" + phone + "\"}"))
                .andExpect(jsonPath("$.code").value(200));
        mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"exp10-phone-b\",\"password\":\"secret123\","
                        + "\"phone\":\"" + phone + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40409))
                .andExpect(jsonPath("$.msg").value("手机号已注册"));
        System.out.println("[exp10-check] duplicate phone -> BusinessException code=40409 PASS");
    }

    @Test
    void unknownExceptionReturnsGenericSystemError() throws Exception {
        mvc.perform(get("/api/exp10/system-error"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(50001))
                .andExpect(jsonPath("$.msg").value("服务器内部错误"));
        System.out.println("[exp10-check] system exception -> generic code=50001 PASS");
    }
}
