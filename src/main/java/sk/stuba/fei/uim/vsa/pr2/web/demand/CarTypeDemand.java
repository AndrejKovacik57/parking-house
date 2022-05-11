package sk.stuba.fei.uim.vsa.pr2.web.demand;

public class CarTypeDemand {
    private Long id;
    private String name;

    public CarTypeDemand() {
    }

    public CarTypeDemand(Long id, String name) {
        this.id = id;
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
}
