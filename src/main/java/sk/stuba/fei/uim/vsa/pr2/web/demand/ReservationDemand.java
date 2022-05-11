package sk.stuba.fei.uim.vsa.pr2.web.demand;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class ReservationDemand {
    private Long id;
    private Date start;
    private Date end;
    private Integer prices;
    private SpotWithoutReservationDemand spot;
    private CarWithoutReservationDemand car;

    public ReservationDemand() {
    }

    public ReservationDemand(Long id, Date start, Date end, Integer prices, SpotWithoutReservationDemand spot, CarWithoutReservationDemand car) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.prices = prices;
        this.spot = spot;
        this.car = car;
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

    public SpotWithoutReservationDemand getSpot() {
        return spot;
    }

    public void setSpot(SpotWithoutReservationDemand spot) {
        this.spot = spot;
    }

    public CarWithoutReservationDemand getCar() {
        return car;
    }

    public void setCar(CarWithoutReservationDemand car) {
        this.car = car;
    }
}
