public class Guest {
    private int guestID;
    private String name;
    private String contactInfo;
    private String address;
    private String bookingHistory = "";
    private String paymentHistory = "";

    public Guest(int guestID, String name, String contactInfo, String address) {
        this.guestID = guestID;
        this.name = name;
        this.contactInfo = contactInfo;
        this.address = address;
    }

    public void updateContactInfo(String newInfo) { this.contactInfo = newInfo; }
    public String getGuestDetails() {
        return "ID: " + guestID + " | Name: " + name + " | Contact: " + contactInfo;
    }

    // getters & setters
    public int getGuestID() { return guestID; }
    public String getName() { return name; }
    public String getContactInfo() { return contactInfo; }
    public String getAddress() { return address; }
    public String getBookingHistory() { return bookingHistory; }
    public String getPaymentHistory() { return paymentHistory; }
    public void addBookingHistory(String s) { bookingHistory += s + "\n"; }
    public void addPaymentHistory(String s) { paymentHistory += s + "\n"; }
}