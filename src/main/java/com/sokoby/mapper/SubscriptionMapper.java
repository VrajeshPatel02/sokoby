package com.sokoby.mapper;

import com.sokoby.entity.Subscription;
import com.sokoby.enums.SubscriptionStatus;
import com.sokoby.payload.SubscriptionDto;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public static SubscriptionDto toDto(Subscription subscription) {
        if (subscription == null) {
            return null;
        }
        SubscriptionDto dto = new SubscriptionDto();
        dto.setId(subscription.getId());
        dto.setMerchant(subscription.getMerchant() != null ? subscription.getMerchant().getId() : null);
        dto.setAmount(subscription.getAmount()); // Assuming Subscription has an amount field; adjust if not
        dto.setInterval(subscription.getInterval()); // Assuming Subscription has an interval field; adjust if not
        dto.setStripeCheckoutSessionId(subscription.getStripeCheckoutSessionId());
        dto.setStripeSubscriptionId(subscription.getStripeSubscriptionId());
        dto.setStatus(subscription.getStatus() != null ? subscription.getStatus().name() : null);
        return dto;
    }

    public static Subscription toEntity(SubscriptionDto dto) {
        if (dto == null) {
            return null;
        }
        Subscription subscription = new Subscription();
        subscription.setId(dto.getId());
        subscription.setAmount(dto.getAmount());
        subscription.setInterval(dto.getInterval());
        return subscription;
    }
}