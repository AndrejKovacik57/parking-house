package sk.stuba.fei.uim.vsa.pr2.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
public class CAR_TYPE  implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "carType")
    private List<PARKING_SPOT> parkingSpots;

    @OneToMany(mappedBy = "carType")
    private List<CAR> cars;

    public CAR_TYPE() {
    }

    public CAR_TYPE(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CAR> getCars() {
        return cars;
    }

    public void setCars(List<CAR> cars) {
        this.cars = cars;
    }

    public List<PARKING_SPOT> getParkingSpots() {
        return parkingSpots;
    }

    public void setParkingSpots(List<PARKING_SPOT> parkingSpots) {
        this.parkingSpots = parkingSpots;
    }

    @Override
    public String toString() {
        return "CAR_TYPE{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", carsSize=" + cars.size() +
                ", cars=" + cars +
                ", parkingSpotsSize=" + parkingSpots.size() +
                ", parkingSpots=" + parkingSpots +
                '}';
    }
}
