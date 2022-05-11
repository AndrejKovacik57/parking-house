package sk.stuba.fei.uim.vsa.pr2.web.response;

import java.util.List;

public class CarResponse {
    private Long id;
    private String vrp;
    private String brand;
    private String model;
    private String colour;
    private List<ReservationResponse> reservations;
    private Long owner;
    private CarTypeResponse type;

    public CarResponse() {
    }

    public CarResponse(Long id, String vrp, String brand, String model, String colour, List<ReservationResponse> reservations, Long owner, CarTypeResponse type) {
        this.id = id;
        this.vrp = vrp;
        this.brand = brand;
        this.model = model;
        this.colour = colour;
        this.reservations = reservations;
        this.owner = owner;
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

    public List<ReservationResponse> getReservations() {
        return reservations;
    }

    public void setReservations(List<ReservationResponse> reservations) {
        this.reservations = reservations;
    }

    public Long getOwner() {
        return owner;
    }

    public void setOwner(Long owner) {
        this.owner = owner;
    }

    public CarTypeResponse getType() {
        return type;
    }

    public void setType(CarTypeResponse type) {
        this.type = type;
    }
}
