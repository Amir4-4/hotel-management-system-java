public class Billing {
    private int guestID;
    private double totalAmount;
    private String paymentHistory = "";
    private boolean isPaid = false;

    public Billing(int guestID) {
        this.guestID = guestID;
    }

    public void generateBill(double amount) { this.totalAmount = amount; }
    public void addPayment(double payment) {
        paymentHistory += "Paid: " + payment + "\n";
        totalAmount -= payment;
        if (totalAmount <= 0) isPaid = true;
    }

    public int getGuestID() { return guestID; }
    public double getTotalAmount() { return totalAmount; }
    public String getPaymentHistory() { return paymentHistory; }
    public boolean getIsPaid() { return isPaid; }
}