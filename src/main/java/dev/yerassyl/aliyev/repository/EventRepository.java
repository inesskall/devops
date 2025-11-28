package dev.yerassyl.aliyev.repository;

import dev.yerassyl.aliyev.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    @Query(value = "SELECT * FROM event  WHERE event.available_from >= ?1 AND event.available_to <= ?2 AND event.ID NOT IN " +
            "(SELECT event_id FROM reservation WHERE (check_in >= ?1 OR check_out <= ?2))", nativeQuery = true)
    List<Event> findAllBetweenDates(@Param("dateFrom") String dateFrom, @Param("dateTo") String dateTo);
    
    // Метод для принудительного применения изменений
    @Override
    void flush();
}
