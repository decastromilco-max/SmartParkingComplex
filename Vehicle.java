public class Vehicle {
    protected String driverName;
    protected String plateNumber;
    protected String vehicleBrand;

    public Vehicle (String driverName, String plateNumber, String vehicleBrand) {
        this.driverName = driverName;
        this.plateNumber = plateNumber;
        this.vehicleBrand = vehicleBrand;
    }

    public String getDriverName() { return driverName; }
    public String getPlateNumber() { return plateNumber; }
    public String getVehicleBrand() { return vehicleBrand; }
}