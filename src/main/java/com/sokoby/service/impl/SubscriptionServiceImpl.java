package com.sokoby.service.impl;

import com.sokoby.entity.Merchant;
import com.sokoby.entity.Store;
import com.sokoby.entity.Subscription;
import com.sokoby.enums.SubscriptionStatus;
import com.sokoby.exception.MerchantException;
import com.sokoby.repository.MerchantRepository;
import com.sokoby.repository.StoreRepository;
import com.sokoby.repository.SubscriptionRepository;
import com.sokoby.service.SubscriptionService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    private final MerchantRepository merchantRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${app.subscription.success.url}") // e.g., http://localhost:8080/subscription/success
    private String successUrl;

    @Value("${app.subscription.cancel.url}") // e.g., http://localhost:8080/subscription/cancel
    private String cancelUrl;

    @Value("${stripe.subscription.monthly.price}") // e.g., price_xxx for $10/month
    private String monthlyPriceId;

    @Value("${stripe.subscription.annual.price}") // e.g., price_yyy for $100/year
    private String annualPriceId;
    private final StoreRepository storeRepository;

    @Autowired
    public SubscriptionServiceImpl(
            MerchantRepository merchantRepository,
            SubscriptionRepository subscriptionRepository,
            StoreRepository storeRepository) {
        this.merchantRepository = merchantRepository;
        this.subscriptionRepository = subscriptionRepository;
        Stripe.apiKey = stripeSecretKey; // Platform’s secret key
        this.storeRepository = storeRepository;
    }

    @Override
    @Transactional
    public String createSubscription(UUID merchantId, boolean isAnnual) throws StripeException {
        Store store = storeRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new MerchantException("Merchant not found", "MERCHANT_NOT_FOUND"));

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(()-> new MerchantException("Merchant not found", "MERCHANT_NOT_FOUND"));
        // Create or retrieve Stripe Customer
        String customerId = store.getStripeAccountId();
        if (customerId == null) {
            Customer customer = Customer.create(
                    CustomerCreateParams.builder()
                            .setEmail(merchant.getEmail())
                            .setName(merchant.getFirstName()+" " + merchant.getLastName())
                            .build()
            );
            store.setStripeAccountId(customer.getId());
            storeRepository.save(store);
            customerId = customer.getId();
            logger.info("Created Stripe customer {} for merchant {}", customerId, merchantId);
        }

        // Create Checkout Session for Subscription
        SessionCreateParams params = SessionCreateParams.builder()
                .setCustomer(customerId)
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(isAnnual ? annualPriceId : monthlyPriceId)
                                .setQuantity(1L)
                                .build()
                )
                .setSuccessUrl(successUrl + "?merchantId=" + merchantId)
                .setCancelUrl(cancelUrl + "?merchantId=" + merchantId)
                .build();

        Session session = Session.create(params);
        logger.info("Created subscription Checkout session {} for merchant {}", session.getId(), merchantId);

        // Create Subscription entity
        Subscription subscription = new Subscription();
        subscription.setMerchant(merchant);
        subscription.setStripeCheckoutSessionId(session.getId());
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscriptionRepository.save(subscription);

        return session.getUrl();
    }
}