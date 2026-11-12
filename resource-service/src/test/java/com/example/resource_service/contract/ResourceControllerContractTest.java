package com.example.resource_service.contract;

import com.example.resource_service.controller.ResourceController;
import com.example.resource_service.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ResourceControllerContractTest {

    @Test
    void getResourceReturnsMp3Bytes() throws Exception {
        ResourceService service = mock(ResourceService.class);
        byte[] mp3 = {1, 2, 3};
        when(service.getResourceById(1)).thenReturn(mp3);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ResourceController(service)).build();

        mockMvc.perform(get("/resources/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "audio/mpeg"))
                .andExpect(content().bytes(mp3));
    }
}