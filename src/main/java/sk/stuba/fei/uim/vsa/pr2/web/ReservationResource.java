package sk.stuba.fei.uim.vsa.pr2.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.stuba.fei.uim.vsa.pr2.domain.RESERVATION;
import sk.stuba.fei.uim.vsa.pr2.domain.USER;
import sk.stuba.fei.uim.vsa.pr2.service.CarParkService;
import sk.stuba.fei.uim.vsa.pr2.web.response.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Path("/")
public class ReservationResource {
    private final CarParkService carParkService = new CarParkService();
    private final ObjectMapper json = new ObjectMapper();

    private USER getUserAuth (String authHEad){
        String base64Encoded = authHEad.substring("Basic ".length());
        String decoded = new String(Base64.getDecoder().decode(base64Encoded));
        String email  = decoded.split(":")[0];
        Object user = carParkService.getUser(email);
        if (user == null)
            return null;
        return (USER) user;
    }

    private ReservationDto createReservationDto(RESERVATION reservation){
        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setId(reservationDto.getId());
        reservationDto.setStart(reservation.getDate());
        reservationDto.setEnd(reservation.getEndDate());
        reservationDto.setPrices(reservation.getParkingCost());
        reservationDto.setCar(reservation.getCar().getId());
        reservationDto.setParkingSpot(reservation.getParkingSpot().getId());
        return reservationDto;
    }
    private List<ReservationDto> getReservationBySpotAndDate(Long spot, Date date){
        List<Object> reservations = carParkService.getReservations(spot,date);
        if (reservations == null)
            return new ArrayList<ReservationDto>();

        List<ReservationDto> reservationsDto= new ArrayList<ReservationDto>();
        reservations.forEach(reservation -> reservationsDto.add(createReservationDto(((RESERVATION) reservation))));

        return reservationsDto;
    }
    private List<ReservationDto> getUserReservations(Long user){
        List<Object> reservations = carParkService.getMyReservations(user);
        if (reservations == null)
            return new ArrayList<ReservationDto>();
        List<ReservationDto> reservationsDto= new ArrayList<ReservationDto>();

        reservations.forEach(reservation -> reservationsDto.add(createReservationDto(((RESERVATION) reservation))));

        return reservationsDto;
    }
    @GET
    @Path("/reservations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReservations(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization,@QueryParam("user") Long user, @QueryParam("spot") Long spot,@QueryParam("date") Date date){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        if ((spot != null && date == null) || (spot == null && date != null)){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        else if (user != null && spot != null){
            List<ReservationDto> reservationsUserDto = getUserReservations(user);
            if (reservationsUserDto.isEmpty())
                return Response.status(Response.Status.NOT_FOUND).build();

            List<ReservationDto> reservationsSpotAndDateDto = getReservationBySpotAndDate(spot, date);
            if (reservationsSpotAndDateDto.isEmpty())
                return Response.status(Response.Status.NOT_FOUND).build();

            List<ReservationDto> intersectionList = new ArrayList<ReservationDto>();
            reservationsUserDto.forEach(userRes -> reservationsSpotAndDateDto.forEach(spotRes -> {
                if(userRes.getId().equals(spotRes.getId()))
                    intersectionList.add(spotRes);
            }));
            return Response.status(Response.Status.OK).entity(intersectionList).build();

        }
        else if (user != null){
            List<ReservationDto> reservationsDto = getUserReservations(user);
            if (reservationsDto.isEmpty())
                return Response.status(Response.Status.NOT_FOUND).build();

            return Response.status(Response.Status.OK).entity(reservationsDto).build();
        }
        else if (spot != null){
            List<ReservationDto> reservationsDto = getReservationBySpotAndDate(spot, date);
            if (reservationsDto.isEmpty())
                return Response.status(Response.Status.NOT_FOUND).build();

            return Response.status(Response.Status.OK).entity(reservationsDto).build();
        }else{
            List<Object> reservations = carParkService.getReservations();
            List<ReservationDto> reservationsDto= new ArrayList<ReservationDto>();
            reservations.forEach(reservation -> reservationsDto.add(createReservationDto(((RESERVATION) reservation))));

            return Response.status(Response.Status.OK).entity(reservationsDto).build();
        }
    }

    @GET
    @Path("/reservations/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCarParks(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization,@PathParam("id") Long id){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Object reservation = carParkService.getReservation(id);
        if (reservation == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        ReservationDto reservationDto = createReservationDto((RESERVATION) reservation);

        return Response.status(Response.Status.OK).entity(reservationDto).build();

    }
    @POST
    @Path("/reservations/{id}/end")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reservationEnd(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id, String body){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Object reservation = carParkService.getReservation(id);
        if (reservation == null)
            return Response.status(Response.Status.BAD_REQUEST).build();

        RESERVATION reservationCasted = (RESERVATION) reservation;
        if(!reservationCasted.getCar().getUser().getId().equals(userAuth.getId()))
            return Response.status(Response.Status.UNAUTHORIZED).build();

        if (!body.equals("{}"))
            return Response.status(Response.Status.BAD_REQUEST).build();

        Object reservationEnd = carParkService.endReservation(id);
        if (reservationEnd == null)
            return Response.status(Response.Status.BAD_REQUEST).build();
        ReservationDto reservationDto = createReservationDto((RESERVATION) reservationEnd);

        return Response.status(Response.Status.OK).entity(reservationDto).build();

    }

    @POST
    @Path("/reservations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createReservation(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization,String body){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        try{
            ReservationDto reservationDto = json.readValue(body, ReservationDto.class);
            Object reservatonCreated = carParkService.createReservation(reservationDto.getParkingSpot(),reservationDto.getCar());
            if (reservatonCreated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            ReservationDto reservatonCreatedDto = createReservationDto((RESERVATION) reservatonCreated);

            return Response.status(Response.Status.OK).entity(reservatonCreatedDto).build();

        }catch (JsonProcessingException e){
            System.err.println(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @PUT
    @Path("/reservations/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateReservation(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization,@PathParam("id") Long id, String body){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        try{
            ReservationDto reservationDto = json.readValue(body, ReservationDto.class);
            RESERVATION reservation = new RESERVATION(reservationDto.getStart());
            reservation.setId(id);

            Object reservationUpdated = carParkService.updateReservation(reservation);
            if (reservationUpdated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            return Response.status(Response.Status.OK).entity(createReservationDto((RESERVATION) reservationUpdated)).build();

        }catch (JsonProcessingException e){
            System.err.println(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}


