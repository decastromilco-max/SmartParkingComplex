import java.sql.*;

public class ParkingSystem {
    private static SmartParkingRepository repo = new SmartParkingRepository();

    public static void viewAvailableSlots() {
        String sql = "SELECT * FROM parkingslot_tbl WHERE status = 'AVAILABLE'";
        try (Connection conn = SmartParkingRepository.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("\n--- Available Slots (Immediate Use) ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("ID: " + rs.getInt("slotId") + " | Slot: " + rs.getString("slotNumber"));
            }
            if (!found) System.out.println("No slots currently available.");
        } catch (SQLException e) {
            System.out.println("Error viewing slots: " + e.getMessage());
        }
    }

    public static boolean reserveSlot(int slotId, String userName, String date) {
        // 1. Check if it's already taken for that date
        if (repo.isSlotReservedOnDate(slotId, date)) {
            System.out.println("Error: Slot " + slotId + " is already reserved for " + date);
            return false;
        }

        // 2. Create the reservation entry
        if (repo.createReservation(slotId, userName, date)) {
            // 3. CRITICAL FIX: Update the status in parkingslot_tbl so it's no longer 'AVAILABLE'
            if (repo.updateSlotStatusById(slotId, "RESERVED")) {
                System.out.println("Success: Slot " + slotId + " has been reserved for " + date + ".");
                return true;
            }
        }
        System.out.println("Error: Reservation failed.");
        return false;
    }

    public static void occupySlot(int slotId) {
        if (repo.updateSlotStatusById(slotId, "Occupied")) {
            System.out.println("Slot Occupied successfully.");
        }
    }

    public static void releaseSlot(int slotId) {
        if (repo.updateSlotStatusById(slotId, "AVAILABLE")) {
            System.out.println("Slot released and now Available.");
        }
    }
}