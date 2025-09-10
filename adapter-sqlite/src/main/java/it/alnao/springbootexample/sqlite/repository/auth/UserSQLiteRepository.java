package it.alnao.springbootexample.sqlite.repository.auth;

import it.alnao.springbootexample.sqlite.entity.auth.UserSQLiteEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per User su SQLite.
 */
@Repository
@Profile("sqlite")
public interface UserSQLiteRepository extends JpaRepository<UserSQLiteEntity, String> {
	@Query("SELECT u FROM UserSQLiteEntity u WHERE u.email = :email")
	Optional<UserSQLiteEntity> findByEmail(@Param("email") String email);

	@Query("SELECT u FROM UserSQLiteEntity u WHERE u.username = :username")
	Optional<UserSQLiteEntity> findByUsername(@Param("username") String username);

	@Query("SELECT u FROM UserSQLiteEntity u WHERE u.email = :email OR u.username = :username")
	Optional<UserSQLiteEntity> findByEmailOrUsername(@Param("email") String email, @Param("username") String username);

	@Query("SELECT u FROM UserSQLiteEntity u WHERE u.enabled = true")
	List<UserSQLiteEntity> findByEnabledTrue();

	@Query("SELECT u FROM UserSQLiteEntity u WHERE u.userRole = :userRole")
	List<UserSQLiteEntity> findByUserRole(@Param("userRole") String userRole);

	@Query("SELECT u FROM UserSQLiteEntity u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
	List<UserSQLiteEntity> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

	@Query("SELECT COUNT(u) FROM UserSQLiteEntity u WHERE u.enabled = true")
	long countByEnabledTrue();

	@Query("SELECT u FROM UserSQLiteEntity u WHERE u.email LIKE %:pattern% OR u.username LIKE %:pattern% OR u.firstName LIKE %:pattern% OR u.lastName LIKE %:pattern%")
	List<UserSQLiteEntity> findByPattern(@Param("pattern") String pattern);
}
