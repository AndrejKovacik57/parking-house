package sk.stuba.fei.uim.vsa.pr2.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
public class PARKING_SPOT implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private Boolean occupied;

    @Column(nullable = false)
    private String  spotIdentifier;

    @ManyToOne
    private CAR_PARK_FLOOR carParkFloor;

    @ManyToOne
    private CAR_TYPE carType;

    @OneToMany(mappedBy = "parkingSpot")
    private List<RESERVATION> reservations;

    public PARKING_SPOT(String spotIdentifier) {
        this.spotIdentifier = spotIdentifier;
        this.occupied = false;
    }

    public PARKING_SPOT() {}


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpotIdentifier() {
        return spotIdentifier;
    }

    public void setSpotIdentifier(String spotIdentifier) {
        this.spotIdentifier = spotIdentifier;
    }

    public CAR_PARK_FLOOR getCarParkFloor() {
        return carParkFloor;
    }

    public void setCarParkFloor(CAR_PARK_FLOOR carParkFloor) {
        this.carParkFloor = carParkFloor;
    }

    public List<RESERVATION> getReservations() {
        return reservations;
    }

    public void setReservations(List<RESERVATION> reservations) {
        this.reservations = reservations;
    }

    public Boolean getOccupied() {
        return occupied;
    }

    public void setOccupied(Boolean occupied) {
        this.occupied = occupied;
    }

    public CAR_TYPE getCarType() {
        return carType;
    }

    public void setCarType(CAR_TYPE carType) {
        this.carType = carType;
    }

    @Override
    public String toString() {
        return "PARKING_SPOT{" +
                "id=" + id +
                ", spotIdentifier='" + spotIdentifier + '\'' +
                ", occupied="+ occupied +
                ", carParkFloorId=" + carParkFloor.getId() +
                ", carType=" + carType.getName() +
                ", reservationsSize=" + reservations.size() +
                ", reservations=" + reservations +
                '}';
    }
}


