package sk.stuba.fei.uim.vsa.pr2.web.demand;

import sk.stuba.fei.uim.vsa.pr2.web.response.CarTypeResponse;

import java.util.List;

public class SpotWithoutReservationDemand {
    private Long id;
    private Boolean free;
    private String identifier;
    private String carParkFloor;
    private CarTypeResponse type;

    public SpotWithoutReservationDemand() {
    }

    public SpotWithoutReservationDemand(Long id, Boolean free, String identifier, String carParkFloor, CarTypeResponse type) {
        this.id = id;
        this.free = free;
        this.identifier = identifier;
        this.carParkFloor = carParkFloor;
        this.type = type;

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
}
