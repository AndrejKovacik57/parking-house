package sk.stuba.fei.uim.vsa.pr2.web.demand;

import java.util.List;

public class UserDemand {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private List<CarDemand> cars;

    public UserDemand() {
    }

    public UserDemand(Long id, String firstName, String lastName, String email, List<CarDemand> cars) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.cars = cars;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<CarDemand> getCars() {
        return cars;
    }

    public void setCars(List<CarDemand> cars) {
        this.cars = cars;
    }
}

