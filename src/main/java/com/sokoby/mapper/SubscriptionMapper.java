package com.sokoby.mapper;

import com.sokoby.entity.Subscription;
import com.sokoby.enums.SubscriptionStatus;
import com.sokoby.payload.SubscriptionDto;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public SubscriptionDto toDto(Subscription subscription) {
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

    public Subscription toEntity(SubscriptionDto dto) {
        if (dto == null) {
            return null;
        }
        Subscription subscription = new Subscription();
        subscription.setId(dto.getId());
        // Merchant is typically set by the service layer using merchantRepository.findById(dto.getMerchant())
        subscription.setAmount(dto.getAmount()); // Assuming Subscription has an amount field; adjust if not
        subscription.setInterval(dto.getInterval()); // Assuming Subscription has an interval field; adjust if not
        subscription.setStripeCheckoutSessionId(dto.getStripeCheckoutSessionId());
        subscription.setStripeSubscriptionId(dto.getStripeSubscriptionId());
        if (dto.getStatus() != null) {
            subscription.setStatus(SubscriptionStatus.valueOf(dto.getStatus()));
        }
        return subscription;
    }
}