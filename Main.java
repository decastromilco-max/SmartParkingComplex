package smartparkinng;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n==== PARKING SYSTEM ====");
            System.out.println("1. View Available Slots");
            System.out.println("2. Reserve Slot");
            System.out.println("3. Car Arrived (Occupy)");
            System.out.println("4. Car Left (Release)");
            System.out.println("5. Exit");
            System.out.print("Choose: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    ParkingSystem.viewAvailableSlots();
                    break;

                case 2:
                    System.out.print("Enter Slot ID: ");
                    int slotId = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter Your Name: ");
                    String name = sc.nextLine();
                    ParkingSystem.reserveSlot(slotId, name);
                    break;

                case 3:
                    System.out.print("Enter Slot ID: ");
                    ParkingSystem.occupySlot(sc.nextInt());
                    break;

                case 4:
                    System.out.print("Enter Slot ID: ");
                    ParkingSystem.releaseSlot(sc.nextInt());
                    break;

                case 5:
                    System.out.println("Exiting...");
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}