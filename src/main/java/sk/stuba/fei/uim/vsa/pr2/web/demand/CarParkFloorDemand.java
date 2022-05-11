package sk.stuba.fei.uim.vsa.pr2.web.demand;

import sk.stuba.fei.uim.vsa.pr2.web.response.ParkingSpotResponse;

import java.util.List;

public class CarParkFloorDemand {
    private String identifier;
    private Long carPark;
    private List<ParkingSpotDemand> spots;

    public CarParkFloorDemand() {
    }

    public CarParkFloorDemand(String identifier, Long carPark, List<ParkingSpotDemand> spots) {
        this.identifier = identifier;
        this.carPark = carPark;
        this.spots = spots;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Long getCarPark() {
        return carPark;
    }

    public void setCarPark(Long carPark) {
        this.carPark = carPark;
    }

    public List<ParkingSpotDemand> getSpots() {
        return spots;
    }

    public void setSpots(List<ParkingSpotDemand> spots) {
        this.spots = spots;
    }
}
