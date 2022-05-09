package sk.stuba.fei.uim.vsa.pr2.web.response;

import java.util.List;

public class CarParkFloorDto {
    private String identifier;
    private Long carPark;
    private List<ParkingSpotDto> spots;

    public CarParkFloorDto() {
    }

    public CarParkFloorDto(String identifier, Long carParkId, List<ParkingSpotDto> parkingSpotsDto) {
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

    public List<ParkingSpotDto> getSpots() {
        return spots;
    }

    public void setSpots(List<ParkingSpotDto> spots) {
        this.spots = spots;
    }
}

