
package dev.yerassyl.aliyev.service;

import dev.yerassyl.aliyev.dto.IdEntity;
import dev.yerassyl.aliyev.dto.SuccessEntity;
import dev.yerassyl.aliyev.entity.Event;

import java.util.List;

public interface EventService {

    List<Event> getEventPagedList(Integer pageNo, Integer pageSize, String sortBy); // Pagination

    List<Event> getAllEvents();

    Event getEvent(Integer id);

    List<Event> getAvailable(String dateFrom, String dateTo);

    IdEntity saveEvent(Event event);

    SuccessEntity deleteEvent(Integer id);

    SuccessEntity patchEvent(Event event);

    void doesReservationOverlap(Event event);

    boolean validateEventExistenceById(Integer id);
}
