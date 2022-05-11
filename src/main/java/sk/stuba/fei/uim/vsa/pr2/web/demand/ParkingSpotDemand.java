package sk.stuba.fei.uim.vsa.pr2.web.demand;

import sk.stuba.fei.uim.vsa.pr2.web.response.CarTypeResponse;
import sk.stuba.fei.uim.vsa.pr2.web.response.ReservationResponse;

import java.util.List;

public class ParkingSpotDemand {
    private Long id;
    private Boolean free;
    private String identifier;
    private String carParkFloor;
    private CarTypeResponse type;
    private List<ReservationDemand> reservations;

    public ParkingSpotDemand() {
    }

    public ParkingSpotDemand(Long id, Boolean free, String identifier, String carParkFloor, CarTypeResponse type, List<ReservationDemand> reservations) {
        this.id = id;
        this.free = free;
        this.identifier = identifier;
        this.carParkFloor = carParkFloor;
        this.type = type;
        this.reservations = reservations;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getFree() {
        return free;
    }

    public void setFree(Boolean free) {
        this.free = free;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getCarParkFloor() {
        return carParkFloor;
    }

    public void setCarParkFloor(String carParkFloor) {
        this.carParkFloor = carParkFloor;
    }

    public CarTypeResponse getType() {
        return type;
    }

    public void setType(CarTypeResponse type) {
        this.type = type;
    }

    public List<ReservationDemand> getReservations() {
        return reservations;
    }

    public void setReservations(List<ReservationDemand> reservations) {
        this.reservations = reservations;
    }
}
