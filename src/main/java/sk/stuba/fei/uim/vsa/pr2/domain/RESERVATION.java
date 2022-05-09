package sk.stuba.fei.uim.vsa.pr2.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class RESERVATION implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date date;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    private Integer parkingCost;

    @ManyToOne
    private CAR car;

    @ManyToOne
    private PARKING_SPOT parkingSpot;

    public RESERVATION() {
    }

    public RESERVATION(Date date, CAR car, PARKING_SPOT parkingSpot) {
        this.date = date;
        this.car = car;
        this.parkingSpot = parkingSpot;
    }
    public RESERVATION(Date date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public CAR getCar() {
        return car;
    }

    public void setCar(CAR car) {
        this.car = car;
    }

    public PARKING_SPOT getParkingSpot() {
        return parkingSpot;
    }

    public void setParkingSpot(PARKING_SPOT parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getParkingCost() {
        return parkingCost;
    }

    public void setParkingCost(Integer parkingCost) {
        this.parkingCost = parkingCost;
    }


    @Override
    public String toString() {
        return "RESERVATION{" +
                "reservationId=" + id +
                ", date=" + date +
                ", endDate=" + endDate +
                ", parkingCost=" + parkingCost +
                ", carId=" + (car == null ? null: car.getId()) +
                ", parkingSpotId=" + (parkingSpot == null ? null: parkingSpot.getId()) +
                '}';
    }
}
