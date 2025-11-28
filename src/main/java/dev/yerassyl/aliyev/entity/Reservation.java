package dev.yerassyl.aliyev.entity;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;


@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reservation")
public class Reservation extends AuditableEntity {

    @Column(name = "event_id", nullable = false)
    private Integer eventId;

    @Column(name = "check_in")
    private String checkIn; // Используется для хранения ID студента

    @Column
    private boolean status;

}
