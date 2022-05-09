package sk.stuba.fei.uim.vsa.pr2.domain;


import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class CAR_PARK_FLOOR_ID implements Serializable {
    private Long carParkId;
    private String floorIdentifier;

    public CAR_PARK_FLOOR_ID() {
    }

    public CAR_PARK_FLOOR_ID(Long carParkId, String floorIdentifier) {
        this.carParkId = carParkId;
        this.floorIdentifier = floorIdentifier;
    }

    public Long getCarParkId() {
        return carParkId;
    }

    public void setCarParkId(Long carParkId) {
        this.carParkId = carParkId;
    }

    public String getFloorIdentifier() {
        return floorIdentifier;
    }

    public void setFloorIdentifier(String floorIdentifier) {
        this.floorIdentifier = floorIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CAR_PARK_FLOOR_ID that = (CAR_PARK_FLOOR_ID) o;

        if (carParkId != null ? !carParkId.equals(that.carParkId) : that.carParkId != null) return false;
        return floorIdentifier != null ? floorIdentifier.equals(that.floorIdentifier) : that.floorIdentifier == null;
    }

    @Override
    public int hashCode() {
        int result = carParkId != null ? carParkId.hashCode() : 0;
        result = 31 * result + (floorIdentifier != null ? floorIdentifier.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CAR_PARK_FLOOR_ID{" +
                "carParkId=" + carParkId +
                ", floorIdentifier='" + floorIdentifier + '\'' +
                '}';
    }
}
