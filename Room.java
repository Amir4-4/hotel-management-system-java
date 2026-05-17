public class Room {
    private int roomID;
    private String roomType;
    private int capacity;
    private boolean isAvailable;
    private double pricePerNight;

    public Room(int roomID, String roomType, int capacity, double pricePerNight) {
        this.roomID = roomID;
        this.roomType = roomType;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
        this.isAvailable = true;
    }

    public void assignRoom() { isAvailable = false; }
    public void releaseRoom() { isAvailable = true; }
    public void updateRoomInfo(String type, double price) {
        this.roomType = type;
        this.pricePerNight = price;
    }

    // getters
    public int getRoomID() { return roomID; }
    public String getRoomType() { return roomType; }
    public int getCapacity() { return capacity; }
    public boolean isAvailable() { return isAvailable; }
    public double getPricePerNight() { return pricePerNight; }
}