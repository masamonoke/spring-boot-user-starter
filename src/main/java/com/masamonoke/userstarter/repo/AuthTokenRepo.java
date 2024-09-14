package com.masamonoke.userstarter.repo;

import com.masamonoke.userstarter.model.AuthToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AuthTokenRepo extends JpaRepository<AuthToken, Long> {
	@Transactional(readOnly = true)
	@Query(value = """
        SELECT t \s
        FROM AuthToken t \s
        INNER JOIN User u ON t.user.id = u.id \s
        WHERE u.id = :id and (t.isExpired = false or t.isRevoked = false) \s
    """)
    List<AuthToken> findAllValidTokenByUser(Long id);

    Optional<AuthToken> findByToken(String token);
}
