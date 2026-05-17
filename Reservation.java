public class Reservation {
    private int reservationID;
    private int guestID;
    private int roomID;
    private String checkInDate;
    private String checkOutDate;
    private String status;

    public Reservation(int reservationID, int guestID, int roomID, String checkInDate, String checkOutDate) {
        this.reservationID = reservationID;
        this.guestID = guestID;
        this.roomID = roomID;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = "Active";
    }

    public void makeReservation() { status = "Active"; }
    public void cancelReservation() { status = "Cancelled"; }
    public void updateReservation(String newCheckOut) { this.checkOutDate = newCheckOut; }

    // getters
    public int getReservationID() { return reservationID; }
    public int getGuestID() { return guestID; }
    public int getRoomID() { return roomID; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public String getStatus() { return status; }
}