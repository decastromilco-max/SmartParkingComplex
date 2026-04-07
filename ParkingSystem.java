package smartparkinng;


import java.sql.*;

public class ParkingSystem {

    // VIEW AVAILABLE SLOTS
    public static void viewAvailableSlots() {
        // Changed to 'AVAILABLE' to match your SQLite database content
        String sql = "SELECT * FROM parking_slots WHERE status = 'AVAILABLE'";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n=== Available Slots ===");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println(
                        "ID: " + rs.getInt("slot_id") +
                                " | Slot: " + rs.getString("slot_number") +
                                " | Status: " + rs.getString("status")
                );
            }
            if (!found) System.out.println("No slots currently available.");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // RESERVE SLOT
    public static void reserveSlot(int slotId, String userName) {
        // Ensuring we use 'parking_slots' (plural)
        String check = "SELECT status FROM parking_slots WHERE slot_id = ?";
        String insert = "INSERT INTO reservation (slot_id, user_name, reservation_time, status) VALUES (?, ?, datetime('now'), 'Reserved')";
        String update = "UPDATE parking_slots SET status = 'Reserved' WHERE slot_id = ?";

        try (Connection conn = DBConnection.connect()) {
            PreparedStatement checkStmt = conn.prepareStatement(check);
            checkStmt.setInt(1, slotId);
            ResultSet rs = checkStmt.executeQuery();

            // Match 'AVAILABLE' casing
            if (rs.next() && rs.getString("status").equalsIgnoreCase("AVAILABLE")) {
                // Insert into reservation table
                PreparedStatement insertStmt = conn.prepareStatement(insert);
                insertStmt.setInt(1, slotId);
                insertStmt.setString(2, userName);
                insertStmt.executeUpdate();

                // Update parking_slots table
                PreparedStatement updateStmt = conn.prepareStatement(update);
                updateStmt.setInt(1, slotId);
                updateStmt.executeUpdate();

                System.out.println("✅ Slot reserved!");
            } else {
                System.out.println("❌ Slot not available.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // MARK AS OCCUPIED (CAR ARRIVED)
    public static void occupySlot(int slotId) {
        // FIXED: Changed 'parking_slot' to 'parking_slots'
        String sql = "UPDATE parking_slots SET status = 'Occupied' WHERE slot_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, slotId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("🚗 Slot is now Occupied.");
            } else {
                System.out.println("❌ Slot ID not found.");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // RELEASE SLOT (CAR LEFT)
    public static void releaseSlot(int slotId) {
        // FIXED: Changed 'parking_slot' to 'parking_slots' and status to 'AVAILABLE'
        String sql = "UPDATE parking_slots SET status = 'AVAILABLE' WHERE slot_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, slotId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("🅿️ Slot is now Available.");
            } else {
                System.out.println("❌ Slot ID not found.");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}