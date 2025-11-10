package dev.yerassyl.aliyev.controller;

import dev.yerassyl.aliyev.constants.AppConstants;
import dev.yerassyl.aliyev.dto.IdEntity;
import dev.yerassyl.aliyev.dto.SuccessEntity;
import dev.yerassyl.aliyev.entity.Event;
import dev.yerassyl.aliyev.service.EventService;
import dev.yerassyl.aliyev.validator.EventValidator;
import dev.yerassyl.aliyev.validator.PageNumberAndSizeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Event Controller containing endpoints of Event related API calls
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class EventController {
    private final EventService eventService;

    /**
     * End point to get all events in the database
     *
     * @return list of events
     */
    @GetMapping(value = "/events", produces = "application/json")
    public ResponseEntity<List<Event>> getEventList(){
        log.info("Get all: {} events from database", eventService.getAllEvents().size());
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    /**
     * End point to get Event paged list
     *
     * @param pageNumber
     * @param pageSize
     * @param sortBy
     * @return
     */
    @GetMapping(value = "/eventPagedList", produces = "application/json")
    public ResponseEntity<List<Event>> getPagedEventList(
            @RequestParam(name = "pageNumber", required = false, defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
            @RequestParam(name = "sortBy", required = false, defaultValue = AppConstants.DEFAULT_SORTING_PARAM) String sortBy) {

        PageNumberAndSizeValidator.validatePageNumberAndSize(pageNumber, pageSize);
        List<Event> eventPagedList = eventService.getEventPagedList(pageNumber, pageSize, sortBy);

        log.info("Return Event paged list with pageNumber: {}, pageSize: {} and sortBy: {}.", pageNumber, pageSize, sortBy);

        return new ResponseEntity<>(eventPagedList, HttpStatus.OK);
    }

    /**
     * End point to get user specified Event
     *
     * @param id Integer
     * @return
     */
    @GetMapping(value = "/event/{id}", produces = "application/json")
    public Event getEvent(@PathVariable Integer id) {
        EventValidator.validateId(id);
        log.info("Get event by id = {}", id);
        return eventService.getEvent(id);
    }

    /**
     * End point to get list of events available between user specified dates
     *
     * @param from String
     * @param to String
     * @return list of events
     */
    @GetMapping(value = "/events/availabilitySearch", produces = "application/json")
    public List<Event> getEvent(@RequestParam("dateFrom") String from, @RequestParam("dateTo") String to){
        EventValidator.validateDates(from, to);
        log.info("Get all events available between dates from: {} to: {}", from, to);
        return eventService.getAvailable(from, to);
    }

    /**
     * End point to update user specified Event.
     *
     * @param event Event
     * @return successEntity
     */
    @PatchMapping(value = "/event", produces = "application/json")
    public SuccessEntity patchEvent(@RequestBody @Valid Event event){
        EventValidator.validateEventPATCH(event);
        log.info("Update Event with name: {}", event.getName());
        return eventService.patchEvent(event);
    }

    /**
     * End point to save a user specified event
     *
     * @param event Event
     * @return idEntity
     */
    @PostMapping(value = "/event", produces = "application/json")
    public IdEntity saveEvent(@RequestBody @Valid Event event){
        EventValidator.validateEventPOST(event);
        log.info("Save a user specified event with name: {}", event.getName());
        return eventService.saveEvent(event);
    }

    /**
     * End point to delete a user specified event
     *
     * @param id Integer
     * @return SuccessEntity
     */
    @DeleteMapping(value = "/event/{id}", produces = "application/json")
    public SuccessEntity deleteEvent(@PathVariable Integer id){
        EventValidator.validateId(id);
        log.info("Delete a user specified event with id = {}", id);
        return eventService.deleteEvent(id);
    }
}
