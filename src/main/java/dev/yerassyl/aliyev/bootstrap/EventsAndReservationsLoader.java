package dev.yerassyl.aliyev.bootstrap;

import dev.yerassyl.aliyev.entity.Event;
import dev.yerassyl.aliyev.entity.Reservation;
import dev.yerassyl.aliyev.entity.ValidTypesOfHotelsEnum;
import dev.yerassyl.aliyev.repository.EventRepository;
import dev.yerassyl.aliyev.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class EventsAndReservationsLoader implements CommandLineRunner {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public void run(String... args) throws Exception {

        if (eventRepository.count() == 0) {
            log.info("Loading data from Events...");
            loadEventObject();
        }

        if (reservationRepository.count() == 0) {
            log.info("Loading data from Reservations...");
            loadReservationObject();
        }


    }

    private void loadReservationObject() {
        Reservation r1 = Reservation.builder()
                .eventId(1)
                .checkIn("2019-01-01")
                .checkOut("2019-12-31")
                .guests(2)
                .build();

        Reservation r2 = Reservation.builder()
                .eventId(2)
                .checkIn("2020-01-01")
                .checkOut("2020-12-31")
                .guests(3)
                .build();

        Reservation r3 = Reservation.builder()
                .eventId(3)
                .checkIn("2021-01-01")
                .checkOut("2021-12-31")
                .guests(4)
                .build();

        Reservation r4 = Reservation.builder()
                .eventId(4)
                .checkIn("2022-01-01")
                .checkOut("2022-12-31")
                .guests(5)
                .build();

        Reservation r5 = Reservation.builder()
                .eventId(5)
                .checkIn("2023-01-01")
                .checkOut("2023-12-31")
                .guests(6)
                .build();

        reservationRepository.save(r1);
        reservationRepository.save(r2);
        reservationRepository.save(r3);
        reservationRepository.save(r4);
        reservationRepository.save(r5);

        log.info("Loaded Reservations: " + reservationRepository.count());
    }

    private void loadEventObject() {
        Event h1 = Event.builder()
                .name("Falkensteiner Event Bratislava")
                .type(ValidTypesOfHotelsEnum.CONCERT)
                .description("Falkensteiner")
                .availableFrom("2019-01-01")
                .availableTo("2019-12-31")
                .status(true)
                .build();

        Event h2 = Event.builder()
                .name("Park Inn by Radisson Danube Bratislava")
                .type(ValidTypesOfHotelsEnum.CONCERT)
                .description("Park Inn")
                .availableFrom("2020-01-01")
                .availableTo("2020-12-31")
                .status(true)
                .build();

        Event h3 = Event.builder()
                .name("Radisson Blu Carlton Event Bratislava")
                .type(ValidTypesOfHotelsEnum.CONCERT)
                .description("Radisson Blu Carlton")
                .availableFrom("2021-01-01")
                .availableTo("2021-12-31")
                .status(true)
                .build();

        Event h4 = Event.builder()
                .name("Boutique Event Bratislava")
                .type(ValidTypesOfHotelsEnum.CONCERT)
                .description("Boutique Event")
                .availableFrom("2022-01-01")
                .availableTo("2022-12-31")
                .status(true)
                .build();

        Event h5 = Event.builder()
                .name("Sheraton Bratislava Event")
                .type(ValidTypesOfHotelsEnum.CONCERT)
                .description("Sheraton")
                .availableFrom("2023-01-01")
                .availableTo("2023-12-31")
                .status(true)
                .build();

        eventRepository.save(h1);
        eventRepository.save(h2);
        eventRepository.save(h3);
        eventRepository.save(h4);
        eventRepository.save(h5);

        log.info("Loaded Events: " + eventRepository.count());
    }
}
