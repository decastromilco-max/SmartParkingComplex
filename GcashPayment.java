public class GCashPayment extends PaymentFramework {
    private String gcashNumber;
    private double walletBalance;

    public GCashPayment(int reservationId, double amount, double discount, String gcashNumber, double balance) {
        super(reservationId, amount, discount);
        this.gcashNumber = gcashNumber;
        this.walletBalance = balance;
    }

    @Override
    public boolean validatePayment() {
        double totalRequired = (this.amount - this.discount) * (1 + VAT_RATE);
        if (this.walletBalance >= totalRequired) {
            this.finalAmount = totalRequired;
            finalizeTransaction(totalRequired);
            return true;
        } else {
            System.out.printf("Error: Insufficient GCash balance. Total is PHP %.2f\n", totalRequired);
            return false;
        }
    }

    @Override
    protected void finalizeTransaction(double finalAmount) {
        this.walletBalance -= finalAmount;
        System.out.println("GCash Number: " + gcashNumber);
        System.out.printf("Amount Deducted: PHP %.2f\n", finalAmount);
        System.out.printf("Remaining Balance: PHP %.2f\n", walletBalance);
        System.out.println("Payment Successful! [GCASH]");
    }
}