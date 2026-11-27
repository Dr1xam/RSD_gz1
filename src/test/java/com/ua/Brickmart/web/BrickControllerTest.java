package com.ua.Brickmart.web;

import com.ua.Brickmart.exception.BrickNotFoundException;
import com.ua.Brickmart.service.BrickService;
import com.ua.Brickmart.web.dto.BrickCardDto;
import com.ua.Brickmart.web.dto.BrickDetailDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrickController.class)
class BrickControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private BrickService brickService;

    @Test
    void list_returnsBricks() throws Exception {
        when(brickService.findAll(null)).thenReturn(List.of(
                new BrickCardDto(1L, "Цегла рядова повнотіла М150", "Керамейя",
                        new BigDecimal("9.50"), "/photos/ryadova-m150.jpg")));

        mockMvc.perform(get("/api/bricks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Цегла рядова повнотіла М150"))
                .andExpect(jsonPath("$[0].manufacturer").value("Керамейя"));
    }

    @Test
    void list_filtersByBrickType() throws Exception {
        when(brickService.findAll("Шамотна")).thenReturn(List.of(
                new BrickCardDto(5L, "Цегла шамотна ША-8", "Вогнетрив",
                        new BigDecimal("27.50"), "/photos/shamotna-sha8.jpg")));

        mockMvc.perform(get("/api/bricks").param("brickType", "Шамотна"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(5));
    }

    @Test
    void getOne_returnsBrick() throws Exception {
        when(brickService.findById(1L)).thenReturn(new BrickDetailDto(1L,
                "Цегла рядова повнотіла М150", "Керамейя", new BigDecimal("9.50"), 3500,
                "/photos/ryadova-m150.jpg", "Класична червона цегла.", "Керамічна"));

        mockMvc.perform(get("/api/bricks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(3500))
                .andExpect(jsonPath("$.brickType").value("Керамічна"));
    }

    @Test
    void getOne_returns404WhenMissing() throws Exception {
        when(brickService.findById(999L)).thenThrow(new BrickNotFoundException(999L));

        mockMvc.perform(get("/api/bricks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returns201() throws Exception {
        when(brickService.create(any())).thenReturn(new BrickDetailDto(6L,
                "Цегла облицювальна", "Євротон", new BigDecimal("14.80"), 2600,
                null, null, "Керамічна"));

        mockMvc.perform(post("/api/bricks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Цегла облицювальна","manufacturer":"Євротон",
                                 "price":14.80,"weight":2600,"brickType":"Керамічна"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(6));
    }
}
