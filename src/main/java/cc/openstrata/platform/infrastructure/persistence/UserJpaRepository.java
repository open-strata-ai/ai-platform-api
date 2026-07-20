package cc.openstrata.platform.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    List<UserEntity> findByTenantId(String tenantId);
    void deleteByTenantId(String tenantId);
}
