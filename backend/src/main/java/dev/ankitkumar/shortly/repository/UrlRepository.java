package dev.ankitkumar.shortly.repository;

import dev.ankitkumar.shortly.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<ShortUrl, Long> {

    boolean existsByShortUrl(String shortCode);
    Optional<ShortUrl> findByShortUrl(String shortCode);
}
