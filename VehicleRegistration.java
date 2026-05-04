public class VehicleRegistration {

    // Pointing to the new unified repository
    private SmartParkingRepository repository = new SmartParkingRepository();

    public void registerVehicle(Vehicle vehicle) {
        if (isInvalid(vehicle.getDriverName()) ||
                isInvalid(vehicle.getPlateNumber()) ||
                isInvalid(vehicle.getVehicleBrand())) {

            System.out.println("ERROR: Registration failed. Required vehicle information is missing or invalid.");
            return;
        }

        // Uses the centralized save method
        boolean isSaved = repository.saveVehicle(vehicle);

        if (isSaved) {
            System.out.println("SUCCESS: The system saved the vehicle details in SmartParkingComplex.db.");
        } else {
            System.out.println("ERROR: Could not complete registration.");
        }
    }

    private boolean isInvalid(String data) {
        return data == null || data.trim().isEmpty();
    }
}