package ru.ion.app.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor

public class Paste {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String key;

    @Column
    private LocalDate createDate;

    @Column
    private LocalDate endDate;


}
