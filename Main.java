import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static Scanner sc = new Scanner(System.in);
    private static VehicleRegistration vehicleService = new VehicleRegistration();
    private static ParkingService bookingService = new ParkingService();
    private static SmartParkingRepository repo = new SmartParkingRepository();
    private static Map<String, User> userDatabase = new HashMap<>();
    private static User currentUser = null;

    public static void main(String[] args) {
        userDatabase.put("admin", new Admin("System Admin", "admin123"));

        boolean systemRunning = true;
        while (systemRunning) {
            System.out.println("\n==========================================");
            System.out.println("   SMART PARKING COMPLEX SYSTEM   ");
            System.out.println("==========================================");
            System.out.println("[1] Register New Account");
            System.out.println("[2] Login");
            System.out.println("[3] Exit System");
            System.out.print("Choose: ");

            String mainChoice = sc.nextLine();
            switch (mainChoice) {
                case "1": handleRegistration(); break;
                case "2":
                    currentUser = handleLogin();
                    if (currentUser != null) runUserSession();
                    break;
                case "3":
                    System.out.println("Exiting. Goodbye!");
                    systemRunning = false;
                    break;
                default: System.out.println("Invalid choice.");
            }
        }
        sc.close();
    }

    private static void handleRegistration() {
        System.out.println("\n--- Create New Account ---");
        System.out.print("Enter Username: ");
        String username = sc.nextLine();
        if (userDatabase.containsKey(username)) {
            System.out.println("Error: Username already exists.");
            return;
        }
        System.out.print("Enter Password: ");
        String password = sc.nextLine();
        System.out.print("Enter Your Full Name: ");
        String name = sc.nextLine();
        userDatabase.put(username, new Customer(name, username, password));
        System.out.println("Registration successful!");
    }

    private static User handleLogin() {
        System.out.println("\n--- Login ---");
        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();
        User user = userDatabase.get(username);
        if (user != null && user.getPassword().equals(password)) {
            System.out.println("Login successful!");
            return user;
        } else {
            System.out.println("Invalid username or password.");
            return null;
        }
    }

    private static void runUserSession() {
        boolean sessionRunning = true;
        while (sessionRunning) {
            if (currentUser.getRole().equals("ADMIN")) {
                sessionRunning = showAdminMenu();
            } else {
                sessionRunning = showCustomerMenu();
            }
        }
    }

    private static boolean showAdminMenu() {
        System.out.println("\n--- Admin Menu ---");
        System.out.println("1. Register Vehicle\n2. Parking Management\n3. Reserve Slot\n4. Book Parking\n5. Cancel Booking\n6. View Income Statement\n7. Logout");
        System.out.print("Choose: ");
        switch (sc.nextLine()) {
            case "1": handleVehicleRegistration(); break;
            case "2": handleParkingManagement(); break;
            case "3": handleReserveSlot(); break;
            case "4": handleBookParking(); break;
            case "5": handleCancelBooking(); break;
            case "6": repo.displayIncomeStatement(); break;
            case "7": return false;
            default: System.out.println("Invalid choice.");
        }
        return true;
    }

    private static boolean showCustomerMenu() {
        System.out.println("\n--- Customer Menu ---");
        System.out.println("1. Register My Vehicle\n2. View Available Slots\n3. Reserve Slot\n4. Book Parking\n5. Cancel My Booking\n6. Logout");
        System.out.print("Choose: ");
        switch (sc.nextLine()) {
            case "1": handleVehicleRegistration(); break;
            case "2": ParkingSystem.viewAvailableSlots(); break;
            case "3": handleReserveSlot(); break;
            case "4": handleBookParking(); break;
            case "5": handleCancelBooking(); break;
            case "6": return false;
            default: System.out.println("Invalid choice.");
        }
        return true;
    }

    // ✅ UPDATED - now checks slot first, uses actual payment amount
    private static void handleReserveSlot() {
        System.out.println("\n--- Reserve Parking Slot ---");
        System.out.print("Enter Slot ID: ");
        int id;
        try {
            id = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid slot ID.");
            return;
        }
        System.out.print("Enter Date (YYYY-MM-DD): ");
        String date = sc.nextLine();

        if (repo.isSlotReservedOnDate(id, date)) {
            System.out.println("Error: Slot " + id + " is already reserved for " + date + ".");
            System.out.println("Returning to menu...");
            return;
        }

        String nameToRegister = currentUser.getName();
        if (currentUser.getRole().equals("ADMIN")) {
            System.out.print("Enter Customer Name: ");
            nameToRegister = sc.nextLine();
        }

        PaymentFramework payment = handlePayment(id);
        if (payment != null) {
            if (ParkingSystem.reserveSlot(id, nameToRegister, date)) {
                repo.recordPayment(id, payment.getFinalAmount());
            } else {
                System.out.printf("Reservation failed. Please contact admin for a refund of PHP %.2f.\n", payment.getFinalAmount());
            }
        }
    }

    // ✅ UPDATED - uses actual payment amount
    private static void handleBookParking() {
        System.out.println("\n--- Book Parking Slot (Immediate) ---");
        System.out.print("Enter Slot Number (e.g. A1): ");
        String slotNum = sc.nextLine();

        PaymentFramework payment = handlePayment(0);
        if (payment != null) {
            repo.recordPayment(0, payment.getFinalAmount());
            bookingService.bookParkingSlot(slotNum);
        }
    }

    private static void handleCancelBooking() {
        System.out.print("\nEnter Slot Number to Cancel: ");
        bookingService.cancelReservationOrBooking(sc.nextLine());
    }

    private static void handleVehicleRegistration() {
        System.out.print("Plate Number: ");
        String plate = sc.nextLine();
        System.out.print("Car Brand: ");
        String brand = sc.nextLine();
        vehicleService.registerVehicle(new Vehicle(currentUser.getName(), plate, brand));
    }

    // ✅ UPDATED - now returns PaymentFramework instead of boolean
    private static PaymentFramework handlePayment(int reservationId) {
        System.out.println("\n--- Payment ---");

        double totalGross = 100.00;
        System.out.println("Total Amount (VAT Inclusive): PHP " + totalGross);

        System.out.print("Discount (e.g. 0.20 for 20%, 0 for none): ");
        double discountRate;
        try {
            discountRate = Double.parseDouble(sc.nextLine());
        } catch (NumberFormatException e) {
            discountRate = 0;
        }

        double discountAmount = totalGross * discountRate;
        double targetTotal = totalGross - discountAmount;
        double netBaseNeeded = targetTotal / 1.12;
        double baseAmount = netBaseNeeded + discountAmount;

        System.out.println("[1] Cash [2] GCash");
        System.out.print("Choose payment method: ");
        String type = sc.nextLine();
        PaymentFramework payment;

        try {
            if (type.equals("1")) {
                System.out.print("Enter cash amount tendered: ");
                double cash = Double.parseDouble(sc.nextLine());
                payment = new CashPayment(reservationId, baseAmount, discountAmount, cash);
            } else if (type.equals("2")) {
                System.out.print("Enter GCash Number: ");
                String num = sc.nextLine();
                System.out.print("Enter GCash wallet balance: ");
                double balance = Double.parseDouble(sc.nextLine());
                payment = new GCashPayment(reservationId, baseAmount, discountAmount, num, balance);
            } else {
                System.out.println("Invalid payment type.");
                return null;
            }

            payment.processInvoice();
            if (payment.validatePayment()) {
                return payment;
            } else {
                return null;
            }

        } catch (Exception e) {
            System.out.println("Error processing payment: " + e.getMessage());
            return null;
        }
    }

    // ✅ UPDATED - try-catch added for Occupy and Release
    private static void handleParkingManagement() {
        System.out.println("\n[1] View Available [2] Occupy [3] Release [4] Back");
        String choice = sc.nextLine();
        switch (choice) {
            case "1" -> ParkingSystem.viewAvailableSlots();
            case "2" -> {
                System.out.print("Slot ID to Occupy: ");
                try {
                    ParkingSystem.occupySlot(Integer.parseInt(sc.nextLine()));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                }
            }
            case "3" -> {
                System.out.print("Slot ID to Release: ");
                try {
                    ParkingSystem.releaseSlot(Integer.parseInt(sc.nextLine()));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                }
            }
        }
    }
}