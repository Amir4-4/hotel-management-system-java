import java.util.ArrayList;

public class HotelManagementSystem {
    public GuestBST guestBST = new GuestBST();
    public ArrayList<Room> roomList = new ArrayList<>();
    public ArrayList<Reservation> reservationList = new ArrayList<>();

    public void addGuest(int id, String name, String contact, String address) {
        guestBST.add(new Guest(id, name, contact, address));
    }

    public void addRoom(Room room) { roomList.add(room); }

    public void makeReservation(int resID, int guestID, int roomID, String checkIn, String checkOut) {
        reservationList.add(new Reservation(resID, guestID, roomID, checkIn, checkOut));
    }

    public String generateReports() {
        return "Guests: " + guestBST.toArrayList().size() +
                "\nRooms: " + roomList.size() +
                "\nReservations: " + reservationList.size();
    }
}
