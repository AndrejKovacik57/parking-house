package sk.stuba.fei.uim.vsa.pr2.web.response;

import java.util.List;

public class CarParkFloorResponse {
    private String identifier;
    private Long carPark;
    private List<ParkingSpotResponse> spots;

    public CarParkFloorResponse() {
    }

    public CarParkFloorResponse(String identifier, Long carParkId, List<ParkingSpotResponse> parkingSpotsDto) {
        this.identifier = identifier;
        this.carPark = carParkId;
        this.spots = parkingSpotsDto;
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

    public List<ParkingSpotResponse> getSpots() {
        return spots;
    }

    public void setSpots(List<ParkingSpotResponse> spots) {
        this.spots = spots;
    }
}

