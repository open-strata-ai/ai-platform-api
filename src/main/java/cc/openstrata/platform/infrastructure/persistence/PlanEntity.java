package cc.openstrata.platform.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** JPA mirror of the plans table (V1__init.sql). quota_template stored as TEXT (JSON). */
@Entity
@Table(name = "plans")
public class PlanEntity {

    @Id
    @Column(name = "plan_id")
    private String planId;

    @Column(nullable = false)
    private String name;

    @Column(name = "quota_template", columnDefinition = "TEXT", nullable = false)
    private String quotaTemplate;

    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getQuotaTemplate() { return quotaTemplate; }
    public void setQuotaTemplate(String quotaTemplate) { this.quotaTemplate = quotaTemplate; }
}
