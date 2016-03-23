package uk.gov.pay.api.model;

public class Payment implements PaymentJSON {
    private final String paymentId;

    private final String paymentProvider;
    private final long amount;
    private final String status;
    private final String description;

    private final String returnUrl;

    private final String reference;

    private final String createdDate;

    public static Payment valueOf(PaymentConnectorResponse paymentConnector) {
        return new Payment(
                paymentConnector.getChargeId(),
                paymentConnector.getAmount(),
                paymentConnector.getStatus(),
                paymentConnector.getReturnUrl(),
                paymentConnector.getDescription(),
                paymentConnector.getReference(),
                paymentConnector.getPaymentProvider(),
                paymentConnector.getCreated_date()
        );
    }

    private Payment(String chargeId, long amount, String status, String returnUrl, String description,
                    String reference, String paymentProvider, String createdDate) {
        this.paymentId = chargeId;
        this.amount = amount;
        this.status = status;
        this.returnUrl = returnUrl;
        this.description = description;
        this.reference = reference;
        this.paymentProvider = paymentProvider;
        this.createdDate = createdDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public long getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId='" + paymentId + '\'' +
                ", paymentProvider='" + paymentProvider + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", description='" + description + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }
}
