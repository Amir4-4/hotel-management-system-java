import java.util.ArrayList;
import java.util.List;

public class ReportGenerator {

    // ---------- RESERVATION REPORT ----------
    public String generateReservationReport(List<Reservation> reservations) {
        // Sort reservations by reservation ID using merge sort
        reservations = mergeSortReservations(reservations);

        StringBuilder sb = new StringBuilder();
        sb.append("Reservation Report:\n");
        for (Reservation r : reservations) {
            sb.append("ResID: ").append(r.getReservationID())
                    .append(" Guest: ").append(r.getGuestID())
                    .append(" Room: ").append(r.getRoomID())
                    .append(" ").append(r.getCheckInDate()).append("->").append(r.getCheckOutDate())
                    .append(" Status: ").append(r.getStatus()).append("\n");
        }
        return sb.toString();
    }

    // ---------- REVENUE REPORT ----------
    public String generateRevenueReport(List<Billing> billings) {
        // Sort billings by total amount
        billings = mergeSortBillings(billings);

        double total = 0;
        for (Billing b : billings) total += b.getTotalAmount();
        return "Revenue Report: total outstanding = " + total;
    }

    // ---------- OCCUPANCY REPORT ----------
    public String generateOccupancyReport(List<Room> rooms, List<Reservation> reservations) {
        // Sort rooms by availability (occupied first)
        rooms = mergeSortRooms(rooms);

        long occupied = rooms.stream().filter(r -> !r.isAvailable()).count();
        return "Occupancy: " + occupied + " / " + rooms.size();
    }

    // ----------------- MERGE SORT HELPERS -----------------

    private List<Reservation> mergeSortReservations(List<Reservation> list) {
        if (list.size() <= 1) return list;

        int mid = list.size() / 2;
        List<Reservation> left = mergeSortReservations(list.subList(0, mid));
        List<Reservation> right = mergeSortReservations(list.subList(mid, list.size()));

        return mergeReservations(left, right);
    }

    private List<Reservation> mergeReservations(List<Reservation> left, List<Reservation> right) {
        List<Reservation> merged = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (left.get(i).getReservationID() <= right.get(j).getReservationID()) {
                merged.add(left.get(i++));
            } else {
                merged.add(right.get(j++));
            }
        }
        while (i < left.size()) merged.add(left.get(i++));
        while (j < right.size()) merged.add(right.get(j++));
        return merged;
    }

    private List<Billing> mergeSortBillings(List<Billing> list) {
        if (list.size() <= 1) return list;

        int mid = list.size() / 2;
        List<Billing> left = mergeSortBillings(list.subList(0, mid));
        List<Billing> right = mergeSortBillings(list.subList(mid, list.size()));

        return mergeBillings(left, right);
    }

    private List<Billing> mergeBillings(List<Billing> left, List<Billing> right) {
        List<Billing> merged = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (left.get(i).getTotalAmount() <= right.get(j).getTotalAmount()) {
                merged.add(left.get(i++));
            } else {
                merged.add(right.get(j++));
            }
        }
        while (i < left.size()) merged.add(left.get(i++));
        while (j < right.size()) merged.add(right.get(j++));
        return merged;
    }

    private List<Room> mergeSortRooms(List<Room> list) {
        if (list.size() <= 1) return list;

        int mid = list.size() / 2;
        List<Room> left = mergeSortRooms(list.subList(0, mid));
        List<Room> right = mergeSortRooms(list.subList(mid, list.size()));

        return mergeRooms(left, right);
    }

    private List<Room> mergeRooms(List<Room> left, List<Room> right) {
        List<Room> merged = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            // Sort by availability: occupied (false) first
            if (!left.get(i).isAvailable() || right.get(j).isAvailable()) {
                merged.add(left.get(i++));
            } else {
                merged.add(right.get(j++));
            }
        }
        while (i < left.size()) merged.add(left.get(i++));
        while (j < right.size()) merged.add(right.get(j++));
        return merged;
    }
}
