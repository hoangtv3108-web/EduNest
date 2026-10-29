package com.edunest.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HelloController.class)
class HelloControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void hello_tra_ve_Hello_World() throws Exception {
        mockMvc.perform(get("/api/hello"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Hello World"));
    }
}
