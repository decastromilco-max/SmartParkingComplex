public abstract class PaymentFramework {
    protected int reservationId;
    protected double amount;
    protected double discount;
    protected final double VAT_RATE = 0.12;
    protected double finalAmount = 0;

    public PaymentFramework(int reservationId, double amount, double discount) {
        this.reservationId = reservationId;
        this.amount = amount;
        this.discount = discount;
    }

    public double getFinalAmount() { return finalAmount; }

    public void processInvoice() {
        double subtotal = amount - discount;
        double vatAmount = subtotal * VAT_RATE;
        double total = subtotal + vatAmount;

        System.out.println("\n----- TRANSACTION INVOICE -----");
        System.out.printf("Base Amount:  PHP %.2f\n", amount);
        System.out.printf("Discount:     -PHP %.2f\n", discount);
        System.out.printf("VAT (12%%):    +PHP %.2f\n", vatAmount);
        System.out.println("--------------------------------");
        System.out.printf("TOTAL DUE:    PHP %.2f\n", total);
        System.out.println("--------------------------------");
    }

    public abstract boolean validatePayment();
    protected abstract void finalizeTransaction(double finalAmount);
}