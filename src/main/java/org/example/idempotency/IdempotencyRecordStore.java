package org.example.idempotency;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public class IdempotencyRecordStore {

    private final JdbcTemplate jdbcTemplate;

    public IdempotencyRecordStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean tryClaim(
            String idempotencyKey,
            String method,
            String path,
            String ownerKey,
            long ttlSeconds
    ) {
        jdbcTemplate.update(
                """
                DELETE FROM dbo.idempotency_keys
                WHERE idempotency_key = ?
                  AND request_method = ?
                  AND request_path = ?
                  AND owner_key = ?
                  AND expires_at <= SYSUTCDATETIME()
                """,
                idempotencyKey, method, path, ownerKey
        );

        try {
            jdbcTemplate.update(
                    """
                    INSERT INTO dbo.idempotency_keys
                        (idempotency_key, request_method, request_path, owner_key,
                         status, expires_at)
                    VALUES (?, ?, ?, ?, N'PROCESSING',
                            DATEADD(SECOND, ?, SYSUTCDATETIME()))
                    """,
                    idempotencyKey, method, path, ownerKey, ttlSeconds
            );
            return true;
        } catch (DuplicateKeyException duplicateKeyException) {
            return false;
        }
    }

    public Optional<IdempotencyRecord> find(
            String idempotencyKey,
            String method,
            String path,
            String ownerKey
    ) {
        return jdbcTemplate.query(
                """
                SELECT status, response_body, expires_at
                FROM dbo.idempotency_keys
                WHERE idempotency_key = ?
                  AND request_method = ?
                  AND request_path = ?
                  AND owner_key = ?
                """,
                resultSet -> {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(new IdempotencyRecord(
                            resultSet.getString("status"),
                            resultSet.getString("response_body"),
                            resultSet.getTimestamp("expires_at").toInstant()
                    ));
                },
                idempotencyKey, method, path, ownerKey
        );
    }

    public void complete(
            String idempotencyKey,
            String method,
            String path,
            String ownerKey,
            String responseBody
    ) {
        jdbcTemplate.update(
                """
                UPDATE dbo.idempotency_keys
                SET status = N'COMPLETED',
                    response_body = ?,
                    completed_at = SYSUTCDATETIME()
                WHERE idempotency_key = ?
                  AND request_method = ?
                  AND request_path = ?
                  AND owner_key = ?
                """,
                responseBody, idempotencyKey, method, path, ownerKey
        );
    }

    public void release(
            String idempotencyKey,
            String method,
            String path,
            String ownerKey
    ) {
        jdbcTemplate.update(
                """
                DELETE FROM dbo.idempotency_keys
                WHERE idempotency_key = ?
                  AND request_method = ?
                  AND request_path = ?
                  AND owner_key = ?
                """,
                idempotencyKey, method, path, ownerKey
        );
    }

    public record IdempotencyRecord(
            String status,
            String responseBody,
            Instant expiresAt
    ) {
    }
}
