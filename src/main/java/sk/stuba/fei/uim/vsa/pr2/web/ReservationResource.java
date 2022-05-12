package sk.stuba.fei.uim.vsa.pr2.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.stuba.fei.uim.vsa.pr2.domain.RESERVATION;
import sk.stuba.fei.uim.vsa.pr2.domain.USER;
import sk.stuba.fei.uim.vsa.pr2.service.CarParkService;
import sk.stuba.fei.uim.vsa.pr2.web.demand.ReservationDemand;
import sk.stuba.fei.uim.vsa.pr2.web.response.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.regex.PatternSyntaxException;

@Path("/")
public class ReservationResource {
    private final CarParkService carParkService = new CarParkService();
    private final ObjectMapper json = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private USER getUserAuth (String authHEad){
        String base64Encoded = authHEad.substring("Basic ".length());
        String decoded = new String(Base64.getDecoder().decode(base64Encoded));
        String[] accountDetails;
        try {
            accountDetails  = decoded.split(":");
        }catch (PatternSyntaxException e){
            return null;
        }

        if (accountDetails.length != 2)
            return null;
        if (!accountDetails[1].matches("-?\\d+(\\.\\d+)?"))
            return null;
        Object user = carParkService.getUser(accountDetails[0]);
        Object user2 = carParkService.getUser((long) Integer.parseInt(accountDetails[1]));
        if (user == null || user2 == null)
            return null;
        USER userCast = (USER) user;
        USER user2Cast = (USER) user2;
        if(!userCast.getId().equals(user2Cast.getId()))
            return null;

        return (USER) user;
    }

    private ReservationResponse createReservationResponse(RESERVATION reservation){
        ReservationResponse reservationResponse = new ReservationResponse();
        reservationResponse.setId(reservation.getId());
        reservationResponse.setStart(reservation.getDate());
        reservationResponse.setEnd(reservation.getEndDate());
        reservationResponse.setPrices(reservation.getParkingCost());
        reservationResponse.setCar(reservation.getCar().getId());
        reservationResponse.setParkingSpot(reservation.getParkingSpot().getId());
        return reservationResponse;
    }
    private List<ReservationResponse> getReservationBySpotAndDate(Long spot, Date date){
        List<Object> reservations = carParkService.getReservations(spot,date);
        if (reservations == null)
            return new ArrayList<ReservationResponse>();

        List<ReservationResponse> reservationsDto= new ArrayList<ReservationResponse>();
        reservations.forEach(reservation -> reservationsDto.add(createReservationResponse(((RESERVATION) reservation))));

        return reservationsDto;
    }
    private List<ReservationResponse> getUserReservations(Long user){
        List<Object> reservations = carParkService.getMyReservations(user);
        if (reservations == null)
            return new ArrayList<ReservationResponse>();
        List<ReservationResponse> reservationsDto= new ArrayList<ReservationResponse>();

        reservations.forEach(reservation -> reservationsDto.add(createReservationResponse(((RESERVATION) reservation))));

        return reservationsDto;
    }
    @GET
    @Path("/reservations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReservations(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization,@QueryParam("user") Long user, @QueryParam("spot") Long spot,@QueryParam("date") String date){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        if ((spot != null && date == null) || (spot == null && date != null)){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        else if (user != null && spot != null){
            Date dateCreated;
            try {
                dateCreated = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            }catch (ParseException e){
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            List<ReservationResponse> reservationsUserResponse = getUserReservations(user);
            List<ReservationResponse> reservationsSpotAndDateResponse = getReservationBySpotAndDate(spot, dateCreated);

            List<ReservationResponse> intersectionList = new ArrayList<ReservationResponse>();
            reservationsUserResponse.forEach(userRes -> reservationsSpotAndDateResponse.forEach(spotRes -> {
                if(userRes.getId().equals(spotRes.getId()))
                    intersectionList.add(spotRes);
            }));
            return Response.status(Response.Status.OK).entity(intersectionList).build();

        }
        else if (user != null){
            List<ReservationResponse> reservationsDto = getUserReservations(user);
            return Response.status(Response.Status.OK).entity(reservationsDto).build();
        }
        else if (spot != null){
            Date dateCreated;
            try {
                dateCreated = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            }catch (ParseException e){
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            List<ReservationResponse> reservationsDto = getReservationBySpotAndDate(spot, dateCreated);
            return Response.status(Response.Status.OK).entity(reservationsDto).build();
        }else{
            List<Object> reservations = carParkService.getReservations();
            List<ReservationResponse> reservationsDto= new ArrayList<ReservationResponse>();
            reservations.forEach(reservation -> reservationsDto.add(createReservationResponse(((RESERVATION) reservation))));

            return Response.status(Response.Status.OK).entity(reservationsDto).build();
        }
    }

    @GET
    @Path("/reservations/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReservations(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization,@PathParam("id") Long id){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        Object reservation = carParkService.getReservation(id);
        if (reservation == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        ReservationResponse reservationResponse = createReservationResponse((RESERVATION) reservation);

        return Response.status(Response.Status.OK).entity(reservationResponse).build();

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

        Object reservationEnd = carParkService.endReservation(id);
        if (reservationEnd == null)
            return Response.status(Response.Status.BAD_REQUEST).build();
        ReservationResponse reservationResponse = createReservationResponse((RESERVATION) reservationEnd);

        return Response.status(Response.Status.OK).entity(reservationResponse).build();

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
            ReservationDemand reservationDemand = json.readValue(body, ReservationDemand.class);
            Object reservatonCreated = carParkService.createReservation(reservationDemand.getSpot().getId(),reservationDemand.getCar().getId());
            if (reservatonCreated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            ReservationResponse reservatonCreatedDto = createReservationResponse((RESERVATION) reservatonCreated);

            return Response.status(Response.Status.CREATED).entity(reservatonCreatedDto).build();

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
            ReservationDemand reservationDemand = json.readValue(body, ReservationDemand.class);
            RESERVATION reservation = new RESERVATION(reservationDemand.getStart());
            reservation.setId(id);

            Object reservationUpdated = carParkService.updateReservation(reservation);
            if (reservationUpdated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            return Response.status(Response.Status.OK).entity(createReservationResponse((RESERVATION) reservationUpdated)).build();

        }catch (JsonProcessingException e){
            System.err.println(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}


