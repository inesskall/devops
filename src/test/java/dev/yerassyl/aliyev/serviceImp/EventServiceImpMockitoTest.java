package dev.yerassyl.aliyev.serviceImp;

import dev.yerassyl.aliyev.entity.Event;
import dev.yerassyl.aliyev.entity.ValidTypesOfHotelsEnum;
import dev.yerassyl.aliyev.repository.EventRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EventServiceImpMockitoTest {
    @Mock
    EventRepository eventRepository;

    @InjectMocks
    EventServiceImp eventServiceImp;

    static List<Event> eventList = new ArrayList<>();

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeAll
    public static void loadEventList() {
        Event event = Event.builder()
                .name("Hilton Event")
                .description("5* Event...")
                .availableFrom("2023-01-01")
                .availableTo("2023-12-31")
                .type(ValidTypesOfHotelsEnum.CONCERT)
                .status(true)
                .build();

        Event event2 = Event.builder()
                .name("Four Season Event")
                .description("5* Event...")
                .availableFrom("2023-01-01")
                .availableTo("2023-12-31")
                .type(ValidTypesOfHotelsEnum.CONCERT)
                .status(true)
                .build();

        eventList.add(event);
        eventList.add(event2);
    }

    @Test
    void getAllHotelsTest_Valid() {

        // when - action or behaviour that we are going test
        when(eventRepository.findAll()).thenReturn(eventList);

        assertEquals(2, eventServiceImp.getAllEvents().size());

        verify(eventRepository, times(1)).findAll();
    }


    @Test
    void saveHotelTest_Valid() {
        Event event3 = Event.builder()
                .name("Hilton Event")
                .description("5* Event...")
                .availableFrom("2023-01-01")
                .availableTo("2023-12-31")
                .type(ValidTypesOfHotelsEnum.CONCERT)
                .status(true)
                .build();

        when(eventRepository.save(event3)).thenReturn(event3);

        // then - verify the result or output using assert statements
        assertAll(
                () -> assertEquals("Hilton Event", event3.getName()),
                () -> assertEquals("5* Event...", event3.getDescription()),
                () -> assertEquals(ValidTypesOfHotelsEnum.CONCERT, event3.getType())
        );
    }


}