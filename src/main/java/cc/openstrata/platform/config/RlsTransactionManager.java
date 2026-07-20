package cc.openstrata.platform.config;

import jakarta.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;

/**
 * Transaction manager that binds the PostgreSQL RLS session variables for every
 * transaction, so the policies in {@code V2__rls.sql} actually enforce tenant
 * isolation in production (R-002). Without this, {@code current_setting('app.tenant_id')}
 * is NULL and the placeholder policy denies all tenant rows.
 *
 * <p>The tenant is taken from {@link TenantContext}, which {@link AuthInterceptor}
 * populates per request. Spring opens the JDBC connection lazily inside the
 * {@code @Transactional} service call — i.e. after the interceptor has run — so
 * setting the variable here, on the same connection/transaction that
 * {@code super.doBegin} just bound, makes {@code SET LOCAL} apply for the whole
 * transaction. Each new transaction re-reads {@link TenantContext}, so concurrent
 * requests stay correctly isolated.
 *
 * <p>platform-admin actors may operate across tenants, so we additionally set
 * {@code app.bypass_rls='on'} (honoured by the policy installed in
 * {@code V5__rls_session.sql}).
 */
public class RlsTransactionManager extends JpaTransactionManager {

    public RlsTransactionManager(EntityManagerFactory emf) {
        super(emf);
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);
        TenantContext ctx = TenantContext.get();
        if (ctx == null) {
            return; // No caller context (e.g. background job): RLS denies by default (fail-closed).
        }
        try {
            Connection conn = DataSourceUtils.getConnection(getDataSource());
            if (ctx.isPlatformAdmin()) {
                execute(conn, "SET LOCAL app.bypass_rls = 'on'");
                if (ctx.getTenantId() != null && isValidTenantId(ctx.getTenantId())) {
                    execute(conn, "SET LOCAL app.tenant_id = '" + ctx.getTenantId() + "'");
                }
                return;
            }
            if (ctx.getTenantId() != null && isValidTenantId(ctx.getTenantId())) {
                execute(conn, "SET LOCAL app.tenant_id = '" + ctx.getTenantId() + "'");
            }
        } catch (SQLException e) {
            throw new TransactionSystemException("Failed to set RLS session variables for tenant scope", e);
        }
    }

    private static void execute(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    /**
     * Tenant ids are header-derived (X-Tenant-Id), so only a safe charset is
     * allowed. Anything else is rejected (fail-closed) rather than interpolated
     * into SQL.
     */
    static boolean isValidTenantId(String tenantId) {
        if (tenantId == null || tenantId.isEmpty() || tenantId.length() > 64) {
            return false;
        }
        for (int i = 0; i < tenantId.length(); i++) {
            char c = tenantId.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9') || c == '.' || c == '_' || c == '-';
            if (!ok) {
                return false;
            }
        }
        return true;
    }
}
