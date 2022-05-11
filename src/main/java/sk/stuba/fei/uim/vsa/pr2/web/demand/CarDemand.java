package sk.stuba.fei.uim.vsa.pr2.web.demand;


import java.util.List;

public class CarDemand {
    private Long id;
    private String vrp;
    private String brand;
    private String model;
    private String colour;
    private List<ReservationDemand> reservations;
    private CarTypeDemand type;

    public CarDemand() {
    }

    public CarDemand(Long id, String vrp, String brand, String model, String colour, List<ReservationDemand> reservations, CarTypeDemand type) {
        this.id = id;
        this.vrp = vrp;
        this.brand = brand;
        this.model = model;
        this.colour = colour;
        this.reservations = reservations;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVrp() {
        return vrp;
    }

    public void setVrp(String vrp) {
        this.vrp = vrp;
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

    public List<ReservationDemand> getReservations() {
        return reservations;
    }

    public void setReservations(List<ReservationDemand> reservations) {
        this.reservations = reservations;
    }

    public CarTypeDemand getType() {
        return type;
    }

    public void setType(CarTypeDemand type) {
        this.type = type;
    }
}


