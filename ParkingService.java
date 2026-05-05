public class ParkingService {
    private SmartParkingRepository repo = new SmartParkingRepository();

    public void bookParkingSlot(String slotNumber) {
        // Business logic: Ask repository to update status
        if (repo.updateSlotStatus(slotNumber, "BOOKED")) {
            System.out.println("Success: Slot " + slotNumber + " booked for immediate use.");
        } else {
            System.out.println("Error: Slot " + slotNumber + " is currently unavailable or booked.");
        }
    }

    public void cancelReservationOrBooking(String slotNumber) {
        // Business logic: Ask repository to handle the multi-table cancellation
        if (repo.performCancellation(slotNumber)) {
            System.out.println("Success: Slot " + slotNumber + " is now AVAILABLE and reservation cancelled.");
        } else {
            // This message will trigger if the slot wasn't found or was already available
            System.out.println("Error: Cancellation failed. Please check the slot number.");
        }
    }
}