package dev.yerassyl.aliyev.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * User Entity для хранения информации о пользователях
 */
@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_table")
public class User extends AuditableEntity {

    @NotBlank(message = "Student ID is mandatory")
    @Size(min = 1, max = 50, message = "Student ID must be between 1 and 50 characters")
    @Column(name = "student_id", unique = true, nullable = false)
    private String studentId;

    @NotBlank(message = "Name is mandatory")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Surname is mandatory")
    @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
    @Column(nullable = false)
    private String surname;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 4, max = 100, message = "Password must be at least 4 characters long")
    @Column(nullable = false)
    private String password;

}

