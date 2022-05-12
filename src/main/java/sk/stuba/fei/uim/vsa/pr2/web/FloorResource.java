package sk.stuba.fei.uim.vsa.pr2.web;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.stuba.fei.uim.vsa.pr2.domain.*;
import sk.stuba.fei.uim.vsa.pr2.web.demand.CarParkFloorDemand;
import sk.stuba.fei.uim.vsa.pr2.web.demand.ParkingSpotDemand;
import sk.stuba.fei.uim.vsa.pr2.web.response.*;
import sk.stuba.fei.uim.vsa.pr2.service.CarParkService;

import java.util.*;
import java.util.regex.PatternSyntaxException;


@Path("/")
public class FloorResource {
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


    private CarParkFloorResponse createCarParkFloorDto(CAR_PARK_FLOOR carParkFloor){
        CarParkFloorResponse carParkFloorResponse = new CarParkFloorResponse();
        carParkFloorResponse.setIdentifier(carParkFloor.getId().getFloorIdentifier());
        carParkFloorResponse.setCarPark(carParkFloor.getCarPark().getId());

        List<ParkingSpotResponse> parkingSpotResponseList = new ArrayList<ParkingSpotResponse>();

        carParkFloor.getParkingSpots().forEach(parkingSpot -> {
            ParkingSpotResponse parkingSpotResponse = new ParkingSpotResponse();
            parkingSpotResponse.setId(parkingSpot.getId());
            parkingSpotResponse.setIdentifier(parkingSpot.getSpotIdentifier());
            parkingSpotResponse.setCarParkFloor(parkingSpot.getCarParkFloor().getId().getFloorIdentifier());
            parkingSpotResponse.setCarPark(parkingSpot.getCarParkFloor().getCarPark().getId());
            parkingSpotResponse.setType(new CarTypeResponse(parkingSpot.getCarType().getId(), parkingSpot.getCarType().getName()));
            parkingSpotResponse.setFree(!parkingSpot.getOccupied());

            List<ReservationResponse> reservationResponseList = new ArrayList<ReservationResponse>();
            parkingSpot.getReservations().forEach(reservation -> {
                ReservationResponse reservationResponse = new ReservationResponse();
                reservationResponse.setId(reservation.getId());
                reservationResponse.setStart(reservation.getDate());
                reservationResponse.setEnd(reservation.getEndDate());
                reservationResponse.setPrices(reservation.getParkingCost());
                reservationResponse.setCar(reservation.getCar().getId());
                reservationResponse.setParkingSpot(reservation.getParkingSpot().getId());
                reservationResponseList.add(reservationResponse);
            });
            parkingSpotResponse.setReservations(reservationResponseList);
            parkingSpotResponseList.add(parkingSpotResponse);
        });
        carParkFloorResponse.setSpots(parkingSpotResponseList);

        return carParkFloorResponse;
    }

    @GET
    @Path("/carparks/{id}/floors")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFloors(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        List<CAR_PARK_FLOOR> carParkFloors = new ArrayList<CAR_PARK_FLOOR>();
        List<Object> objectArraylist = carParkService.getCarParkFloors(id);
        if(objectArraylist == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        objectArraylist.forEach(floor -> {
            carParkFloors.add((CAR_PARK_FLOOR) floor);
        });
        List<CarParkFloorResponse> carParkFloorResponseArrayList = new ArrayList<CarParkFloorResponse>();

        carParkFloors.forEach(floor ->{
            CarParkFloorResponse carParkFloorResponse = createCarParkFloorDto(floor);
            carParkFloorResponseArrayList.add(carParkFloorResponse);
        });

        return Response.status(Response.Status.OK).entity(carParkFloorResponseArrayList).build();

    }

    @GET
    @Path("/carparks/{id}/floors/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFloors(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization,@PathParam("id") Long id, @PathParam("identifier") String identifier){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Object carParkFloor = carParkService.getCarParkFloor(id, identifier);
        if (carParkFloor == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        CarParkFloorResponse carParkFloorResponse = createCarParkFloorDto((CAR_PARK_FLOOR) carParkFloor);

        return Response.status(Response.Status.OK).entity(carParkFloorResponse).build();

    }

    @POST
    @Path("/carparks/{id}/floors")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createFloors(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization,@PathParam("id") Long id, String body){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        try{
            CarParkFloorDemand carParkFloorDemand= json.readValue(body, CarParkFloorDemand.class);


            Object carParkFloorCreated = carParkService.createCarParkFloor(id,carParkFloorDemand.getIdentifier());

            if (carParkFloorCreated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            CAR_PARK_FLOOR carParkFloorCreatedCast = (CAR_PARK_FLOOR) carParkFloorCreated;

            if (carParkFloorDemand.getSpots() != null)
                for (ParkingSpotDemand parkingSpot :carParkFloorDemand.getSpots()){

                    if(parkingSpot.getType() == null )
                        return Response.status(Response.Status.BAD_REQUEST).build();

                    Object carType;
                    if(parkingSpot.getType().getId() == null && parkingSpot.getType().getName() != null){
                        carType = carParkService.getCarType(parkingSpot.getType().getName());
                        if (carType == null)
                            return Response.status(Response.Status.BAD_REQUEST).build();
                    }

                    else if(parkingSpot.getType().getName() == null)
                        return Response.status(Response.Status.BAD_REQUEST).build();

                    else
                        carType = carParkService.getCarType(parkingSpot.getType().getId());

                    CAR_TYPE carTypeCasted;
                    Boolean typeCreated;
                    if (carType == null){
                        carTypeCasted = (CAR_TYPE) carParkService.createCarType(parkingSpot.getType().getName());
                        typeCreated = Boolean.TRUE;
                    }

                    else{
                        carTypeCasted = (CAR_TYPE) carType;
                        typeCreated = Boolean.FALSE;
                    }

                    Object parkingSpotCreated = carParkService.createParkingSpot(id, parkingSpot.getCarParkFloor(),parkingSpot.getIdentifier(), carTypeCasted.getId());
                    if (parkingSpotCreated == null){
                        if (typeCreated)
                            carParkService.deleteCarType(carTypeCasted.getId());

                        carParkService.deleteCarParkFloor(carParkFloorDemand.getCarPark(),carParkFloorDemand.getIdentifier());
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                }

            CAR_PARK_FLOOR carParkFloorCreatedWhole = (CAR_PARK_FLOOR) carParkService.getCarParkFloor(carParkFloorCreatedCast.getCarPark().getId(),carParkFloorCreatedCast.getId().getFloorIdentifier());
            return Response.status(Response.Status.CREATED).entity(createCarParkFloorDto(carParkFloorCreatedWhole)).build();

        }catch (JsonProcessingException e){
            System.err.println(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

    }
//      mam kompozitny kluc
//    @PUT
//    @Path("/carparks/{id}/floors/{identifier}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response updateCarParkFloor(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization,@PathParam("id") Long id, @PathParam("identifier") String identifier, String body){
//        }
//    }

    @DELETE
    @Path("/carparks/{id}/floors/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCarParks(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization,@PathParam("id") Long id, @PathParam("identifier") String identifier){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Object carparkFloorDeleted = carParkService.deleteCarParkFloor(id,identifier);
        if (carparkFloorDeleted == null)
            return Response.status(Response.Status.BAD_REQUEST).build();

        return Response.status(Response.Status.NO_CONTENT ).build();

    }

}
