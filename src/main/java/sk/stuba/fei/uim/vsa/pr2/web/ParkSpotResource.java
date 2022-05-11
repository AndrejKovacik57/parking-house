package sk.stuba.fei.uim.vsa.pr2.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.stuba.fei.uim.vsa.pr2.domain.*;
import sk.stuba.fei.uim.vsa.pr2.service.CarParkService;
import sk.stuba.fei.uim.vsa.pr2.web.demand.ParkingSpotDemand;
import sk.stuba.fei.uim.vsa.pr2.web.response.*;

import java.util.*;

@Path("/")
public class ParkSpotResource {
    private final CarParkService carParkService = new CarParkService();
    private final ObjectMapper json = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private USER getUserAuth (String authHEad){
        String base64Encoded = authHEad.substring("Basic ".length());
        String decoded = new String(Base64.getDecoder().decode(base64Encoded));
        String[] accountDetails  = decoded.split(":");
        if (accountDetails.length != 2)
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


    private ParkingSpotResponse createCarParkingSpotDto(PARKING_SPOT parkingSpot){
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
        return parkingSpotResponse;
    }

    private Map<String, List<ParkingSpotResponse>> objMapToDtoMap(Map<String, List<Object>> objMap){
        Map<String, List<ParkingSpotResponse>> parkingSpots = new HashMap<String, List<ParkingSpotResponse>>();
        for (Map.Entry<String,List<Object>> entry : objMap.entrySet()){
//            entry.getKey(), entry.getValue()
            List<ParkingSpotResponse> spotsList = new ArrayList<ParkingSpotResponse>();
            entry.getValue().forEach(parkingSpot -> {
                spotsList.add(createCarParkingSpotDto((PARKING_SPOT) parkingSpot));
            });
            parkingSpots.put(entry.getKey(), spotsList);
        }
        return parkingSpots;
    }

    @GET
    @Path("/carparks/{id}/spots")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSpots(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id, @QueryParam("free") Boolean free){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Map<String, List<Object>> parkingSpotsObjectMap;
        Object carPark = carParkService.getCarPark(id);
        if(carPark == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        if (free != null){
            String carParkName = ((CAR_PARK)carPark).getName();
            if(free){
                parkingSpotsObjectMap = carParkService.getAvailableParkingSpots(carParkName);
            }
            else{
                parkingSpotsObjectMap = carParkService.getOccupiedParkingSpots(carParkName);
            }
        }else{
            parkingSpotsObjectMap = carParkService.getParkingSpots(id);

        }
        if(parkingSpotsObjectMap == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        Map<String, List<ParkingSpotResponse>> parkingSpots = objMapToDtoMap(parkingSpotsObjectMap);
        List<ParkingSpotResponse> parkingSpotResponseList = new ArrayList<ParkingSpotResponse>();
        parkingSpots.values().forEach(parkingSpotResponseList::addAll);

        return Response.status(Response.Status.OK).entity(parkingSpotResponseList).build();

    }
    @GET
    @Path("/carparks/{id}/floors/{identifier}/spots")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSpots(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id, @PathParam("identifier") String identifier){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        List<PARKING_SPOT> parkingSpotArrayList = new ArrayList<PARKING_SPOT>();
        List<Object> objectArrayList = carParkService.getParkingSpots(id, identifier);
        if (objectArrayList == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        objectArrayList.forEach(parkingSpot -> {
            parkingSpotArrayList.add((PARKING_SPOT) parkingSpot);
        });
        List<ParkingSpotResponse> parkingSpotsDtoArrayList = new ArrayList<ParkingSpotResponse>();

        parkingSpotArrayList.forEach(parkingSpot ->{
            ParkingSpotResponse parkingSpotResponse = createCarParkingSpotDto(parkingSpot);
            parkingSpotsDtoArrayList.add(parkingSpotResponse);
        });

        return Response.status(Response.Status.OK).entity(parkingSpotsDtoArrayList).build();
    }

    @GET
    @Path("/parkingspots/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSpots(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Object parkingSpot = carParkService.getParkingSpot(id);
        if (parkingSpot == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        ParkingSpotResponse parkingSpotResponse = createCarParkingSpotDto((PARKING_SPOT) parkingSpot);

        return Response.status(Response.Status.OK).entity(parkingSpotResponse).build();

    }

    @POST
    @Path("/carparks/{id}/floors/{identifier}/spots")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSpots(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id, @PathParam("identifier") String identifier, String body){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        try{
            ParkingSpotDemand parkingSpotResponse = json.readValue(body, ParkingSpotDemand.class);

            if(parkingSpotResponse.getType() == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            if(parkingSpotResponse.getType().getName() == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            Object carType = carParkService.getCarType(parkingSpotResponse.getType().getName());
            CAR_TYPE carTypeCasted;
            Boolean typeCreated;
            if (carType == null){
                carTypeCasted = (CAR_TYPE) carParkService.createCarType(parkingSpotResponse.getType().getName());
                typeCreated = Boolean.TRUE;
            }

            else{
                carTypeCasted = (CAR_TYPE) carType;
                typeCreated = Boolean.FALSE;
            }

            Object parkingSpotCreated = carParkService.createParkingSpot(id, identifier, parkingSpotResponse.getIdentifier(), carTypeCasted.getId());
            if (parkingSpotCreated == null){
                if (typeCreated)
                    carParkService.deleteCarType(carTypeCasted.getId());
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            PARKING_SPOT parkingSpotCreatedCast = (PARKING_SPOT)parkingSpotCreated;
            PARKING_SPOT ParkingSpotCreatedWhole = (PARKING_SPOT) carParkService.getParkingSpot(parkingSpotCreatedCast.getId());
            return Response.status(Response.Status.CREATED).entity(createCarParkingSpotDto(ParkingSpotCreatedWhole)).build();

        }catch (JsonProcessingException e){
            System.err.println(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @PUT
    @Path("/parkingspots/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSpot(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id, String body){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        try{
            ParkingSpotResponse parkingSpotResponse = json.readValue(body, ParkingSpotResponse.class);
            PARKING_SPOT parkingSpot =  new PARKING_SPOT(parkingSpotResponse.getIdentifier());
            parkingSpot.setId(id);
            Object spotUpdated = carParkService.updateParkingSpot(parkingSpot);
            if (spotUpdated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            return Response.status(Response.Status.OK).entity(createCarParkingSpotDto((PARKING_SPOT) spotUpdated)).build();

        }catch (JsonProcessingException e){
            System.err.println(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    @DELETE
    @Path("/parkingspots/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCarParks(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Object carparkDeleted = carParkService.deleteParkingSpot(id);
        if (carparkDeleted == null)
            return Response.status(Response.Status.BAD_REQUEST).build();

        return Response.status(Response.Status.NO_CONTENT).build();

    }

}
