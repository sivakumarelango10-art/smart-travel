package com.smarttravel.modules.notification.repository;

import com.smarttravel.modules.notification.model.PushSubscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionRepository extends MongoRepository<PushSubscription, String> {

    List<PushSubscription> findByUserIdAndActiveTrue(String userId);

    Optional<PushSubscription> findByUserIdAndEndpoint(String userId, String endpoint);

    Optional<PushSubscription> findByEndpoint(String endpoint);

    void deleteByUserIdAndEndpoint(String userId, String endpoint);

    void deleteByEndpoint(String endpoint);
}
