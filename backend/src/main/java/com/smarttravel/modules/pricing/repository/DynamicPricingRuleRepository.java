package com.smarttravel.modules.pricing.repository;

import com.smarttravel.modules.pricing.model.DynamicPricingRule;
import com.smarttravel.modules.pricing.model.DynamicPricingRuleType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for dynamic pricing rules.
 */
public interface DynamicPricingRuleRepository extends MongoRepository<DynamicPricingRule, String> {

    List<DynamicPricingRule> findByEnabledTrueOrderByPriorityAsc();

    List<DynamicPricingRule> findByTypeAndEnabledTrue(DynamicPricingRuleType type);

    List<DynamicPricingRule> findByTypeAndEnabledTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            DynamicPricingRuleType type, Instant refDate1, Instant refDate2);
}
