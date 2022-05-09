package sk.stuba.fei.uim.vsa.pr2.web.response;

import java.util.List;

public class ParkingSpotDto {
    private Long id;
    private Boolean free;
    private String identifier;
    private String carParkFloor;
    private Long carPark;
    private CarTypeDto type;
    private List<ReservationDto> reservations;

    public ParkingSpotDto() {
    }

    public ParkingSpotDto(Long id, Boolean free, String identifier, String carParkFloor, Long carPark, CarTypeDto type, List<ReservationDto>  reservations) {
        this.id = id;
        this.free = free;
        this.identifier = identifier;
        this.carParkFloor = carParkFloor;
        this.carPark = carPark;
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

    public Long getCarPark() {
        return carPark;
    }

    public void setCarPark(Long carPark) {
        this.carPark = carPark;
    }

    public CarTypeDto getType() {
        return type;
    }

    public void setType(CarTypeDto type) {
        this.type = type;
    }

    public List<ReservationDto> getReservations() {
        return reservations;
    }

    public void setReservations(List<ReservationDto> reservations) {
        this.reservations = reservations;
    }
}
