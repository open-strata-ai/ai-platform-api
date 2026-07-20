package cc.openstrata.platform.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cc.openstrata.platform.domain.Application;
import cc.openstrata.platform.domain.AppId;
import cc.openstrata.platform.domain.Entitlement;
import cc.openstrata.platform.domain.ModelGrant;
import cc.openstrata.platform.domain.Quota;
import cc.openstrata.platform.domain.QuotaId;
import cc.openstrata.platform.domain.ResourceDimension;
import cc.openstrata.platform.domain.Tenant;
import cc.openstrata.platform.domain.TenantId;
import cc.openstrata.platform.domain.TenantStatus;
import cc.openstrata.platform.domain.User;
import cc.openstrata.platform.domain.UserId;
import cc.openstrata.platform.domain.Role;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JpaTenantRepositoryTest {

    private TenantJpaRepository tenantRepo;
    private UserJpaRepository userRepo;
    private ApplicationJpaRepository appRepo;
    private QuotaJpaRepository quotaRepo;
    private EntitlementJpaRepository entitlementRepo;
    private ModelGrantJpaRepository modelGrantRepo;
    private JpaTenantRepository repo;

    @BeforeEach
    void setUp() {
        tenantRepo = mock(TenantJpaRepository.class);
        userRepo = mock(UserJpaRepository.class);
        appRepo = mock(ApplicationJpaRepository.class);
        quotaRepo = mock(QuotaJpaRepository.class);
        entitlementRepo = mock(EntitlementJpaRepository.class);
        modelGrantRepo = mock(ModelGrantJpaRepository.class);
        repo = new JpaTenantRepository(tenantRepo, userRepo, appRepo, quotaRepo,
                entitlementRepo, modelGrantRepo);
    }

    @Test
    void savePersistsTenantAndSubEntities() {
        Tenant t = new Tenant(new TenantId("t1"), "Test Tenant");
        t.activate();
        User u = new User(new UserId("u1"), new TenantId("t1"), "user@test.com", Role.DEVELOPER);
        t.addUser(u);

        when(tenantRepo.findById("t1")).thenReturn(Optional.empty());

        repo.save(t);

        verify(tenantRepo).save(any(TenantEntity.class));
        verify(userRepo).deleteByTenantId("t1");
        verify(userRepo).save(any(UserEntity.class));
    }

    @Test
    void findByIdReconstructsTenantFromEntities() {
        TenantEntity tenantEntity = new TenantEntity();
        tenantEntity.setTenantId("t1");
        tenantEntity.setName("Test Tenant");
        tenantEntity.setStatus("ACTIVE");
        tenantEntity.setCreatedAt(Instant.now());
        tenantEntity.setUpdatedAt(Instant.now());

        UserEntity userEntity = new UserEntity();
        userEntity.setUserId("u1");
        userEntity.setTenantId("t1");
        userEntity.setEmail("user@test.com");
        userEntity.setRole("DEVELOPER");
        userEntity.setStatus("ACTIVE");

        when(tenantRepo.findById("t1")).thenReturn(Optional.of(tenantEntity));
        when(userRepo.findByTenantId("t1")).thenReturn(List.of(userEntity));
        when(appRepo.findByTenantId("t1")).thenReturn(List.of());
        when(quotaRepo.findByTenantId("t1")).thenReturn(List.of());
        when(entitlementRepo.findByTenantId("t1")).thenReturn(List.of());
        when(modelGrantRepo.findByTenantId("t1")).thenReturn(List.of());

        Optional<Tenant> result = repo.findById(new TenantId("t1"));

        assertTrue(result.isPresent());
        assertEquals("t1", result.get().getTenantId().value());
        assertEquals("Test Tenant", result.get().getName());
        assertEquals(TenantStatus.ACTIVE, result.get().getStatus());
        assertEquals(1, result.get().getUsers().size());
        assertEquals("u1", result.get().getUsers().get(0).getUserId().value());
        assertEquals(Role.DEVELOPER, result.get().getUsers().get(0).getRole());
    }

    @Test
    void existsDelegatesToJpa() {
        when(tenantRepo.existsById("t1")).thenReturn(true);
        assertTrue(repo.exists(new TenantId("t1")));
        verify(tenantRepo).existsById("t1");
    }

    @Test
    void findAllReturnsAllTenants() {
        TenantEntity e1 = new TenantEntity();
        e1.setTenantId("t1");
        e1.setName("T1");
        e1.setStatus("ACTIVE");
        e1.setCreatedAt(Instant.now());
        e1.setUpdatedAt(Instant.now());

        when(tenantRepo.findAll()).thenReturn(List.of(e1));
        when(userRepo.findByTenantId("t1")).thenReturn(List.of());
        when(appRepo.findByTenantId("t1")).thenReturn(List.of());
        when(quotaRepo.findByTenantId("t1")).thenReturn(List.of());
        when(entitlementRepo.findByTenantId("t1")).thenReturn(List.of());
        when(modelGrantRepo.findByTenantId("t1")).thenReturn(List.of());

        List<Tenant> all = repo.findAll();
        assertEquals(1, all.size());
        assertEquals("t1", all.get(0).getTenantId().value());
    }

    @Test
    void saveUpdatesExistingTenant() {
        TenantEntity existing = new TenantEntity();
        existing.setTenantId("t1");
        existing.setName("Old Name");
        existing.setCreatedAt(Instant.now());
        existing.setUpdatedAt(Instant.now());

        when(tenantRepo.findById("t1")).thenReturn(Optional.of(existing));

        Tenant t = new Tenant(new TenantId("t1"), "New Name");
        t.activate();
        repo.save(t);

        verify(tenantRepo).save(argThat(entity ->
            "t1".equals(entity.getTenantId()) && "New Name".equals(entity.getName())
        ));
    }
}
