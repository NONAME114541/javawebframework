package com.cdutetc.nanpo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cdutetc.nanpo.dao.MenuRepository;

@SpringBootTest
@AutoConfigureMockMvc
class MenuRestControllerTests {
    @Autowired MockMvc mvc;
    @Autowired MenuRepository menus;
    private final List<Long> cleanup = new ArrayList<>();

    @AfterEach
    void cleanup() { menus.deleteAllById(cleanup); }

    private long create(String name, String price) throws Exception {
        var result = mvc.perform(post("/api/menus").contentType(MediaType.APPLICATION_JSON)
                .content("{\"dishName\":\"" + name + "\",\"price\":" + price + "}"))
                .andExpect(status().isCreated()).andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/menus/")))
                .andExpect(jsonPath("$.code").value(200)).andReturn();
        String location = result.getResponse().getHeader("Location");
        long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));cleanup.add(id);return id;
    }

    @Test
    void crudAndAllBindingModesReturnUniformResult() throws Exception {
        long id = create("exp09-麻辣豆腐", "19.50");
        mvc.perform(get("/api/menus/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)).andExpect(jsonPath("$.msg").value("操作成功"))
                .andExpect(jsonPath("$.data.dishName").value("exp09-麻辣豆腐"));
        mvc.perform(get("/api/menus").param("page","0").param("size","5").param("keyword","exp09"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.records[0].id").value(id))
                .andExpect(jsonPath("$.data.page").value(0)).andExpect(jsonPath("$.data.size").value(5));
        mvc.perform(put("/api/menus/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"dishName\":\"exp09-家常豆腐\",\"price\":22.00}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.dishName").value("exp09-家常豆腐"));
        mvc.perform(delete("/api/menus/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        cleanup.remove(id);
        mvc.perform(get("/api/menus/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40401));
        System.out.println("[exp09-check] POST/GET list/GET detail/PUT/DELETE + PathVariable/RequestParam/RequestBody PASS");
    }

    @Test
    void commaSeparatedIdsBindToLongListForBatchDelete() throws Exception {
        long first=create("exp09-batch-a","8.00"),second=create("exp09-batch-b","9.00");
        mvc.perform(delete("/api/menus").param("ids", first + "," + second))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(2));
        cleanup.remove(first);cleanup.remove(second);
        assertThat(menus.existsById(first)).isFalse();
        assertThat(menus.existsById(second)).isFalse();
        System.out.println("[exp09-check] @RequestParam List<Long> ids batch binding and delete PASS");
    }

    @Test
    void malformedParametersAndBodiesUseUniformFailureEnvelope() throws Exception {
        mvc.perform(get("/api/menus/not-a-number")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40001));
        mvc.perform(delete("/api/menus")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40001));
        mvc.perform(post("/api/menus").contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40001));
        mvc.perform(post("/api/menus").contentType(MediaType.APPLICATION_JSON)
                .content("{\"dishName\":\"\",\"price\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40001));
        System.out.println("[exp09-check] type/missing/body/value binding failures -> uniform code=40001 PASS");
    }

    @Test
    void threeNonRestUrlsAreRejectedAndCorrectRoutesRemainAvailable() throws Exception {
        mvc.perform(get("/getMenuById").param("id","1")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40404));
        mvc.perform(post("/createMenu").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40404));
        mvc.perform(delete("/deleteMenu").param("id","1")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(40404));
        mvc.perform(get("/api/menus")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        System.out.println("[exp09-check] wrong URLs rejected: /getMenuById, /createMenu, /deleteMenu; REST routes PASS");
    }
}
