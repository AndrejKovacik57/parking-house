package sk.stuba.fei.uim.vsa.pr2.web.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class ReservationDto {
    private Long id;
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date start;
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date end;
    private Integer prices;
    private Long car;
    private Long parkingSpot;

    public ReservationDto() {
    }

    public ReservationDto(Long id, Date start, Date end, Integer prices, Long car, Long parkingSpot) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.prices = prices;
        this.car = car;
        this.parkingSpot = parkingSpot;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Integer getPrices() {
        return prices;
    }

    public void setPrices(Integer prices) {
        this.prices = prices;
    }

    public Long getCar() {
        return car;
    }

    public void setCar(Long car) {
        this.car = car;
    }

    public Long getParkingSpot() {
        return parkingSpot;
    }

    public void setParkingSpot(Long parkingSpot) {
        this.parkingSpot = parkingSpot;
    }
}
