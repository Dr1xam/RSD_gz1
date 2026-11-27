package com.ua.Brickmart.service;

import com.ua.Brickmart.domain.Brick;
import com.ua.Brickmart.exception.BrickNotFoundException;
import com.ua.Brickmart.repository.BrickRepository;
import com.ua.Brickmart.service.mapper.BrickMapper;
import com.ua.Brickmart.web.dto.BrickCardDto;
import com.ua.Brickmart.web.dto.BrickDetailDto;
import com.ua.Brickmart.web.dto.BrickRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BrickServiceImplTest {

    private final BrickRepository repository = mock(BrickRepository.class);
    private final BrickMapper mapper = new BrickMapper();
    private final BrickService service = new BrickServiceImpl(repository, mapper);

    private Brick sampleBrick() {
        return new Brick(1L, "Цегла силікатна М200", "Сілікат-Буд",
                new BigDecimal("8.20"), 3700, "/photos/sylikatna-m200.jpg",
                "Біла цегла з вапна та піску.", "Силікатна");
    }

    @Test
    void findById_returnsBrick() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleBrick()));

        BrickDetailDto dto = service.findById(1L);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Цегла силікатна М200");
        assertThat(dto.brickType()).isEqualTo("Силікатна");
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(BrickNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void findAll_withoutType_returnsAllBricks() {
        when(repository.findAll()).thenReturn(List.of(sampleBrick()));

        List<BrickCardDto> cards = service.findAll(null);

        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).manufacturer()).isEqualTo("Сілікат-Буд");
        verify(repository, never()).findByBrickType(any());
    }

    @Test
    void findAll_withType_filtersByType() {
        when(repository.findByBrickType("Силікатна")).thenReturn(List.of(sampleBrick()));

        List<BrickCardDto> cards = service.findAll("Силікатна");

        assertThat(cards).extracting(BrickCardDto::name).containsExactly("Цегла силікатна М200");
        verify(repository, never()).findAll();
    }

    @Test
    void create_resetsIdAndSaves() {
        BrickRequest request = new BrickRequest("Клінкерна цегла Графіт", "Клінкер Плюс",
                new BigDecimal("32.00"), 2900, "Клінкерна", "/photos/klinker-grafit.jpg", "Фасадна");
        when(repository.save(any(Brick.class))).thenAnswer(inv -> {
            Brick b = inv.getArgument(0);
            assertThat(b.getId()).isNull();
            b.setId(10L);
            return b;
        });

        BrickDetailDto dto = service.create(request);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.price()).isEqualByComparingTo("32.00");
    }

    @Test
    void update_throwsWhenNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        BrickRequest request = new BrickRequest("Нова", "Хтось",
                new BigDecimal("1.00"), 1000, "Керамічна", null, null);

        assertThatThrownBy(() -> service.update(42L, request))
                .isInstanceOf(BrickNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(repository.deleteById(7L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOf(BrickNotFoundException.class);
    }
}
