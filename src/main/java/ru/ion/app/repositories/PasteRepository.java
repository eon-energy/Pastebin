package ru.ion.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ion.app.entitys.Paste;

import java.util.Optional;

public interface PasteRepository extends JpaRepository<Paste, Long> {
    Optional<Paste> findByKey(String link);
    void deleteByKey(String link);
}
