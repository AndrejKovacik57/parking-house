package sk.stuba.fei.uim.vsa.pr2.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
public class CAR_PARK implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false)
    private String address;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private Integer pricePerHour;

    @OneToMany(mappedBy = "carPark",orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<CAR_PARK_FLOOR> floors;

    public CAR_PARK(String address, String name, Integer pricePerHour) {
        this.address = address;
        this.name = name;
        this.pricePerHour = pricePerHour;
    }

    public CAR_PARK() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(Integer pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public List<CAR_PARK_FLOOR> getFloors() {
        return floors;
    }

    public void setFloors(List<CAR_PARK_FLOOR> carParkFloors) {
        this.floors = carParkFloors;
    }

    @Override
    public String toString() {
        return "CAR_PARK{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", name='" + name + '\''  +
                ", pricePerHour=" + pricePerHour +
                ", carParkFloorsSize=" + floors.size() +
                ", carParkFloors=" + floors +
                '}';
    }
}
