package sk.stuba.fei.uim.vsa.pr2.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
public class CAR_PARK_FLOOR implements Serializable {
    @EmbeddedId
    @Column(nullable = false)
    private CAR_PARK_FLOOR_ID id;

    @ManyToOne
    @MapsId("carParkId")
    private CAR_PARK carPark;

    @OneToMany(mappedBy = "carParkFloor", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<PARKING_SPOT> parkingSpots;

    public CAR_PARK_FLOOR(CAR_PARK_FLOOR_ID id, CAR_PARK carPark) {
        this.id = id;
        this.carPark = carPark;
    }

    public CAR_PARK_FLOOR() {
    }

    public CAR_PARK_FLOOR(CAR_PARK_FLOOR_ID id) {
        this.id = id;
    }

    public CAR_PARK getCarPark() {
        return carPark;
    }

    public CAR_PARK_FLOOR_ID getId() {
        return id;
    }

    public void setId(CAR_PARK_FLOOR_ID id) {
        this.id = id;
    }

    public void setCarPark(CAR_PARK carPark) {
        this.carPark = carPark;
    }

    public List<PARKING_SPOT> getParkingSpots() {
        return parkingSpots;
    }

    public void setParkingSpots(List<PARKING_SPOT> parkingSpots) {
        this.parkingSpots = parkingSpots;
    }


    @Override
    public String toString() {
        return "CAR_PARK_FLOOR{" +
                "id=" + id +
                ", carParkId=" + carPark.getId() +
                ", parkingSpotsSize=" + parkingSpots.size() +
                ", parkingSpots=" + parkingSpots +
                '}';
    }
}
