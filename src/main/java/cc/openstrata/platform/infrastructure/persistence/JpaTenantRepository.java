package cc.openstrata.platform.infrastructure.persistence;

import cc.openstrata.platform.domain.Application;
import cc.openstrata.platform.domain.AppId;
import cc.openstrata.platform.domain.AgentSpecRef;
import cc.openstrata.platform.domain.Entitlement;
import cc.openstrata.platform.domain.ModelGrant;
import cc.openstrata.platform.domain.PlanId;
import cc.openstrata.platform.domain.Quota;
import cc.openstrata.platform.domain.QuotaId;
import cc.openstrata.platform.domain.ResourceDimension;
import cc.openstrata.platform.domain.Role;
import cc.openstrata.platform.domain.Tenant;
import cc.openstrata.platform.domain.TenantId;
import cc.openstrata.platform.domain.TenantStatus;
import cc.openstrata.platform.domain.User;
import cc.openstrata.platform.domain.UserId;
import cc.openstrata.platform.domain.UserStatus;
import cc.openstrata.platform.domain.port.TenantRepository;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/** JPA-backed TenantRepository. Activated by @Profile("prod"). */
public class JpaTenantRepository implements TenantRepository {

    private final TenantJpaRepository tenantRepo;
    private final UserJpaRepository userRepo;
    private final ApplicationJpaRepository appRepo;
    private final QuotaJpaRepository quotaRepo;
    private final EntitlementJpaRepository entitlementRepo;
    private final ModelGrantJpaRepository modelGrantRepo;

    public JpaTenantRepository(TenantJpaRepository tenantRepo, UserJpaRepository userRepo,
                               ApplicationJpaRepository appRepo, QuotaJpaRepository quotaRepo,
                               EntitlementJpaRepository entitlementRepo,
                               ModelGrantJpaRepository modelGrantRepo) {
        this.tenantRepo = tenantRepo;
        this.userRepo = userRepo;
        this.appRepo = appRepo;
        this.quotaRepo = quotaRepo;
        this.entitlementRepo = entitlementRepo;
        this.modelGrantRepo = modelGrantRepo;
    }

    @Override
    @Transactional
    public void save(Tenant tenant) {
        String tid = tenant.getTenantId().value();
        Instant now = Instant.now();

        // Upsert tenant root
        TenantEntity entity = tenantRepo.findById(tid).orElseGet(() -> {
            TenantEntity e = new TenantEntity();
            e.setCreatedAt(now);
            return e;
        });
        entity.setTenantId(tid);
        entity.setName(tenant.getName());
        entity.setStatus(tenant.getStatus().name());
        entity.setPlanId(tenant.getPlanId() != null ? tenant.getPlanId().value() : null);
        entity.setMultitenancy(tenant.isMultitenancyEnabled());
        entity.setUpdatedAt(now);
        tenantRepo.save(entity);

        // Replace sub-entities: delete old, insert new
        userRepo.deleteByTenantId(tid);
        for (User u : tenant.getUsers()) {
            UserEntity ue = new UserEntity();
            ue.setUserId(u.getUserId().value());
            ue.setTenantId(tid);
            ue.setEmail(u.getEmail());
            ue.setRole(u.getRole().name());
            ue.setStatus(u.getStatus().name());
            ue.setKcId(u.getKcId());
            userRepo.save(ue);
        }

        appRepo.deleteByTenantId(tid);
        for (Application a : tenant.getApplications()) {
            ApplicationEntity ae = new ApplicationEntity();
            ae.setAppId(a.getAppId().value());
            ae.setTenantId(tid);
            ae.setName(a.getName());
            ae.setAgentSpecRef(a.getAgentSpecRef() != null ? a.getAgentSpecRef().toRef() : null);
            appRepo.save(ae);
        }

        quotaRepo.deleteByTenantId(tid);
        for (Quota q : tenant.getQuotas()) {
            QuotaEntity qe = new QuotaEntity();
            qe.setQuotaId(q.getQuotaId().value());
            qe.setTenantId(tid);
            qe.setDimension(q.getDimension().name());
            qe.setLimitVal(q.getLimit());
            qe.setUsedVal(q.getUsed());
            quotaRepo.save(qe);
        }

        entitlementRepo.deleteByTenantId(tid);
        for (Entitlement e : tenant.getEntitlements()) {
            EntitlementEntity ee = new EntitlementEntity();
            ee.setTenantId(tid);
            ee.setComponent(e.getComponent());
            ee.setAllowed(e.isAllowed());
            entitlementRepo.save(ee);
        }

        modelGrantRepo.deleteByTenantId(tid);
        for (ModelGrant mg : tenant.getModelGrants()) {
            ModelGrantEntity me = new ModelGrantEntity();
            me.setTenantId(tid);
            me.setProvider(mg.getProvider());
            me.setModel(mg.getModel());
            modelGrantRepo.save(me);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> findById(TenantId id) {
        return tenantRepo.findById(id.value()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(TenantId id) {
        return tenantRepo.existsById(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tenant> findAll() {
        return tenantRepo.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivate(Object obj, String fieldName) {
        try {
            Field f = findField(obj.getClass(), fieldName);
            f.setAccessible(true);
            return (T) f.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("Cannot access " + fieldName + " on "
                    + obj.getClass().getSimpleName(), e);
        }
    }

    private <T> void setPrivate(Object obj, String fieldName, T value) {
        try {
            Field f = findField(obj.getClass(), fieldName);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Cannot set " + fieldName + " on "
                    + obj.getClass().getSimpleName(), e);
        }
    }

    private Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), name);
            }
            throw e;
        }
    }

    private Tenant toDomain(TenantEntity e) {
        String tid = e.getTenantId();
        Tenant t = new Tenant(new TenantId(tid), e.getName());
        setPrivate(t, "status", TenantStatus.valueOf(e.getStatus()));
        if (e.getPlanId() != null) {
            t.assignPlan(new PlanId(e.getPlanId()));
        }
        if (e.isMultitenancy()) {
            t.enableMultitenancy();
        }

        // Users
        for (UserEntity ue : userRepo.findByTenantId(tid)) {
            User u = new User(new UserId(ue.getUserId()), new TenantId(tid),
                    ue.getEmail(), Role.valueOf(ue.getRole()));
            setPrivate(u, "status", UserStatus.valueOf(ue.getStatus()));
            if (ue.getKcId() != null) {
                u.setKcId(ue.getKcId());
            }
            t.addUser(u);
        }

        // Applications
        for (ApplicationEntity ae : appRepo.findByTenantId(tid)) {
            AgentSpecRef ref = ae.getAgentSpecRef() != null
                    ? AgentSpecRef.parse(ae.getAgentSpecRef()) : null;
            t.registerApplication(new Application(
                    new AppId(ae.getAppId()), new TenantId(tid), ae.getName(), ref));
        }

        // Quotas
        for (QuotaEntity qe : quotaRepo.findByTenantId(tid)) {
            t.setQuota(ResourceDimension.valueOf(qe.getDimension()), qe.getLimitVal());
        }

        // Entitlements
        for (EntitlementEntity ee : entitlementRepo.findByTenantId(tid)) {
            t.setEntitlement(ee.getComponent(), ee.isAllowed());
        }

        // ModelGrants
        for (ModelGrantEntity me : modelGrantRepo.findByTenantId(tid)) {
            t.grantModel(me.getProvider(), me.getModel());
        }

        return t;
    }
}
