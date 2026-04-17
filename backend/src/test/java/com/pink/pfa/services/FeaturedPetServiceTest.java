package com.pink.pfa.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pink.pfa.controllers.requests.FeaturedPetRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.FeaturedPets;
import com.pink.pfa.models.Pet;
import com.pink.pfa.models.datatransfer.FeaturedPetDTO;
import com.pink.pfa.repos.FeaturedPetsRepository;
import com.pink.pfa.repos.PetRepository;

@ExtendWith(MockitoExtension.class)
class FeaturedPetServiceTest {

    @Mock
    private FeaturedPetsRepository featuredPetRepository;

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private FeaturedPetService featuredPetService;

    private Pet mockPet;
    private FeaturedPets mockFeaturedPet;

    @BeforeEach
    void seedPets() {
        mockPet = new Pet();
        mockPet.setPetId(1);

        mockFeaturedPet = new FeaturedPets(
            LocalDate.now(),
            LocalDate.now().plusDays(1),
            "Test reason"
        );
        mockFeaturedPet.setPet(mockPet);
    }

    // --- findAll ---------------------------------------------------------------

    @Test
    void findAll_returnsMappedDTOList() {
        when(featuredPetRepository.findAll()).thenReturn(List.of(mockFeaturedPet));

        List<FeaturedPetDTO> result = featuredPetService.findAll();

        assertThat(result).hasSize(1);
        verify(featuredPetRepository).findAll();
    }

    @Test
    void findAll_returnsEmptyList_whenNoFeaturedPets() {
        when(featuredPetRepository.findAll()).thenReturn(List.of());

        List<FeaturedPetDTO> result = featuredPetService.findAll();

        assertThat(result).isEmpty();
    }

    // --- addFPet ---------------------------------------------------------------

    @Test
    void addFPet_savesAndReturnsDTO_whenPetExists() {
        FeaturedPetRequest request = new FeaturedPetRequest(1, "Chosen manually");
        when(petRepository.findById(1)).thenReturn(Optional.of(mockPet));
        when(featuredPetRepository.save(any(FeaturedPets.class))).thenReturn(mockFeaturedPet);

        FeaturedPetDTO result = featuredPetService.addFPet(request);

        assertThat(result).isNotNull();
        verify(petRepository).findById(1);
        verify(featuredPetRepository).save(any(FeaturedPets.class));
    }

    @Test
    void addFPet_throwsResourceNotFoundException_whenPetNotFound() {
        FeaturedPetRequest request = new FeaturedPetRequest(99, "Broken by design");
        when(petRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> featuredPetService.addFPet(request))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(featuredPetRepository, never()).save(any());
    }

    // --- addFRandomPetByType ---------------------------------------------------

    @Test
    void addFRandomPetByType_savesAndReturnsDTO_whenActivePetExists() {
        when(petRepository.findByPetStatusNotAndTypeRand("Dog", "INACTIVE"))
            .thenReturn(Optional.of(mockPet));
        when(featuredPetRepository.save(any(FeaturedPets.class))).thenReturn(mockFeaturedPet);

        FeaturedPetDTO result = featuredPetService.addFRandomPetByType("Dog", "Chosen randomly");

        assertThat(result).isNotNull();
        verify(petRepository).findByPetStatusNotAndTypeRand("Dog", "INACTIVE");
        verify(featuredPetRepository).save(any(FeaturedPets.class));
    }

    @Test
    void addFRandomPetByType_throwsResourceNotFoundException_whenNoActivePetsExist() {
        when(petRepository.findByPetStatusNotAndTypeRand("Cat", "INACTIVE"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> featuredPetService.addFRandomPetByType("Cat", "Chosen randomly"))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(featuredPetRepository, never()).save(any());
    }

    // --- findById --------------------------------------------------------------

    @Test
    void findById_returnsDTO_whenFeaturedPetExists() {
        when(featuredPetRepository.findById(1)).thenReturn(Optional.of(mockFeaturedPet));

        FeaturedPetDTO result = featuredPetService.findById(1);

        assertThat(result).isNotNull();
        verify(featuredPetRepository).findById(1);
    }

    @Test
    void findById_throwsResourceNotFoundException_whenNotFound() {
        when(featuredPetRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> featuredPetService.findById(99))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- deleteByPetId ---------------------------------------------------------

    @Test
    void deleteByPetId_callsRepositoryDelete() {
        featuredPetService.deleteByPetId(1);

        verify(featuredPetRepository).deleteByPet_PetId(1);
    }

    @Test
    void deleteByPetId_doesNotThrow_whenPetDoesNotExist() {
        doNothing().when(featuredPetRepository).deleteByPet_PetId(99);

        assertThatCode(() -> featuredPetService.deleteByPetId(99))
            .doesNotThrowAnyException();
    }
}