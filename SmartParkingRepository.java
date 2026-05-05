import java.sql.*;

public class SmartParkingRepository {
    private static final String DB_URL = "jdbc:sqlite:SmartParkingComplex-1.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // --- VEHICLE OPERATIONS ---
    public boolean saveVehicle(Vehicle vehicle) {
        String sql = "INSERT INTO tbl_spc(DriverName, PlateNumber, VehicleBrand) VALUES(?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vehicle.getDriverName());
            pstmt.setString(2, vehicle.getPlateNumber());
            pstmt.setString(3, vehicle.getVehicleBrand());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DAO Error (SaveVehicle): " + e.getMessage());
            return false;
        }
    }

    // --- PARKING SLOT OPERATIONS ---
    public boolean updateSlotStatus(String slotNumber, String status) {
        String checkSql = "SELECT status FROM parkingslot_tbl WHERE slotNumber = ?";
        String updateSql = "UPDATE parkingslot_tbl SET status = ? WHERE slotNumber = ?";

        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, slotNumber.toUpperCase());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && "AVAILABLE".equalsIgnoreCase(rs.getString("status"))) {
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, status);
                    updateStmt.setString(2, slotNumber.toUpperCase());
                    return updateStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("DAO Error (UpdateStatus): " + e.getMessage());
        }
        return false;
    }

    public boolean updateSlotStatusById(int slotId, String status) {
        String sql = "UPDATE parkingslot_tbl SET status = ? WHERE slotId = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, slotId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DAO Error (UpdateById): " + e.getMessage());
            return false;
        }
    }

    // --- RESERVATION OPERATIONS ---
    public boolean isSlotReservedOnDate(int slotId, String date) {
        String sql = "SELECT COUNT(*) FROM reservation_tbl WHERE slot_id = ? AND reservation_date = ? AND status = 'Reserved'";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, slotId);
            pstmt.setString(2, date);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("DAO Error (CheckDate): " + e.getMessage());
        }
        return false;
    }

    public boolean createReservation(int slotId, String userName, String date) {
        String sql = "INSERT INTO reservation_tbl (slot_id, user_name, reservation_date, reservation_time, status) " +
                "VALUES (?, ?, ?, datetime('now'), 'Reserved')";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, slotId);
            pstmt.setString(2, userName);
            pstmt.setString(3, date);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DAO Error (CreateRes): " + e.getMessage());
            return false;
        }
    }

    // --- CANCELLATION LOGIC (Centralized SQL) ---
    public boolean performCancellation(String slotNumber) {
        String updateSlotSql = "UPDATE parkingslot_tbl SET status = 'AVAILABLE' WHERE slotNumber = ?";
        String updateResSql = "UPDATE reservation_tbl SET status = 'Cancelled' WHERE slot_id = " +
                "(SELECT slotId FROM parkingslot_tbl WHERE slotNumber = ?) " +
                "AND status != 'Cancelled'";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false); // Start transaction for data integrity
            try (PreparedStatement st1 = conn.prepareStatement(updateSlotSql);
                 PreparedStatement st2 = conn.prepareStatement(updateResSql)) {

                st1.setString(1, slotNumber.toUpperCase());
                st2.setString(1, slotNumber.toUpperCase());

                int rowsUpdated = st1.executeUpdate();
                st2.executeUpdate();

                if (rowsUpdated > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("DAO Error (Cancellation): " + e.getMessage());
            return false;
        }
    }

    // --- PAYMENT & INCOME METHODS ---
    public void recordPayment(int reservationId, double amount) {
        String sql = "INSERT INTO payment_tbl(reservation_id, amount_paid, payment_date) VALUES(?, ?, date('now'))";
        try (Connection conn = connect(); PreparedStatement pstnt = conn.prepareStatement(sql)) {
            pstnt.setInt(1, reservationId);
            pstnt.setDouble(2, amount);
            pstnt.executeUpdate();
            System.out.println("Payment successfully recorded in the database.");
        } catch (SQLException e) {
            System.err.println("Error recording payment: " + e.getMessage());
        }
    }

    public void displayIncomeStatement() {
        String sql = "SELECT SUM(amount_paid) as total, COUNT(payment_id) as count FROM payment_tbl";
        try (Connection conn = connect();
             PreparedStatement pstnt = conn.prepareStatement(sql);
             ResultSet rs = pstnt.executeQuery()) { // rs must be initialized HERE

            if (rs.next()) {
                System.out.println("\n===== INCOME STATEMENT =====");
                System.out.println("Total Transactions: " + rs.getInt("count"));
                System.out.printf("Total Revenue: PHP %.2f\n", rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching income: " + e.getMessage());
        }
    }
}