package sk.stuba.fei.uim.vsa.pr2.web.response;


import java.util.List;

public class CarParkDto {
    private Long id;
    private String address;
    private String name;
    private Integer prices;
    private List<CarParkFloorDto> floors;

    public CarParkDto() {
    }

    public CarParkDto(Long id, String address, String name, Integer pricePerHour, List<CarParkFloorDto> floors) {
        this.id = id;
        this.address = address;
        this.name = name;
        this.prices = pricePerHour;
        this.floors = floors;
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

    public Integer getPrices() {
        return prices;
    }

    public void setPrices(Integer prices) {
        this.prices = prices;
    }

    public List<CarParkFloorDto> getFloors() {
        return floors;
    }

    public void setFloors(List<CarParkFloorDto> carParkFloorsDto) {
        this.floors = carParkFloorsDto;
    }
}
