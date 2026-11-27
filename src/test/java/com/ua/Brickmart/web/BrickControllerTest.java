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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    void getOne_returns404WithErrorBody() throws Exception {
        when(brickService.findById(999L)).thenThrow(new BrickNotFoundException(999L));

        mockMvc.perform(get("/api/bricks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Цеглину з id=999 не знайдено"))
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    void update_returns404WhenMissing() throws Exception {
        when(brickService.update(eq(42L), any())).thenThrow(new BrickNotFoundException(42L));

        mockMvc.perform(put("/api/bricks/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Цегла","manufacturer":"Керамейя",
                                 "price":9.50,"weight":3500,"brickType":"Керамічна"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void delete_returns404WhenMissing() throws Exception {
        doThrow(new BrickNotFoundException(7L)).when(brickService).delete(7L);

        mockMvc.perform(delete("/api/bricks/7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_returns400WhenNameBlankAndPriceNegative() throws Exception {
        mockMvc.perform(post("/api/bricks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","manufacturer":"Євротон",
                                 "price":-5,"weight":2600,"brickType":"Керамічна"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Помилка валідації"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details.length()").value(2));

        verify(brickService, never()).create(any());
    }

    @Test
    void create_returns400WhenBodyMalformed() throws Exception {
        mockMvc.perform(post("/api/bricks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void getOne_returns400WhenIdNotNumber() throws Exception {
        mockMvc.perform(get("/api/bricks/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
