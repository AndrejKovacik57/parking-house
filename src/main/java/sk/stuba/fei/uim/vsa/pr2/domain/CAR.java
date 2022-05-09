package sk.stuba.fei.uim.vsa.pr2.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
public class CAR  implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;
    @Column(unique = true, nullable = false)
    private String vehicleRegistrationPlate;
    @Column(nullable = false)
    private String brand;
    @Column(nullable = false)
    private String model;
    @Column(nullable = false)
    private String colour;

    @OneToMany(mappedBy = "car")
    private List<RESERVATION> reservations;

    @ManyToOne
    private USER user;

    @ManyToOne
    private CAR_TYPE carType;

    public CAR(String vehicleRegistrationPlate, String brand, String model, String colour) {
        this.vehicleRegistrationPlate = vehicleRegistrationPlate;
        this.brand = brand;
        this.model = model;
        this.colour = colour;
    }

    public CAR() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleRegistrationPlate() {
        return vehicleRegistrationPlate;
    }

    public void setVehicleRegistrationPlate(String vehicleRegistrationPlate) {
        this.vehicleRegistrationPlate = vehicleRegistrationPlate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public List<RESERVATION> getReservations() {
        return reservations;
    }

    public void setReservations(List<RESERVATION> reservations) {
        this.reservations = reservations;
    }

    public USER getUser() {
        return user;
    }

    public void setUser(USER user) {
        this.user = user;
    }


    public CAR_TYPE getCarType() {
        return carType;
    }

    public void setCarType(CAR_TYPE carType) {
        this.carType = carType;
    }

    @Override
    public String toString() {
        return "CAR{" +
                "id=" + id +
                ", registrationNumber='" + vehicleRegistrationPlate + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", colour='" + colour + '\'' +
                ", carType=" + carType.getName() +
                ", reservationsSize=" + reservations.size() +
                ", reservations=" + reservations +
                ", userId=" + user.getId() +
                '}';
    }
}

