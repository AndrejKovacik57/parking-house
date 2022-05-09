package sk.stuba.fei.uim.vsa.pr2.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.stuba.fei.uim.vsa.pr2.domain.*;
import sk.stuba.fei.uim.vsa.pr2.service.CarParkService;
import sk.stuba.fei.uim.vsa.pr2.web.response.*;

import java.util.*;

@Path("/")
public class ParkSpotResource {
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

    private ParkingSpotDto createCarParkingSpotDto(PARKING_SPOT parkingSpot){
        ParkingSpotDto parkingSpotDto = new ParkingSpotDto();
        parkingSpotDto.setId(parkingSpot.getId());
        parkingSpotDto.setIdentifier(parkingSpot.getSpotIdentifier());
        parkingSpotDto.setCarParkFloor(parkingSpot.getCarParkFloor().getId().getFloorIdentifier());
        parkingSpotDto.setCarPark(parkingSpot.getCarParkFloor().getCarPark().getId());
        parkingSpotDto.setType(new CarTypeDto(parkingSpot.getCarType().getId(), parkingSpot.getCarType().getName()));
        parkingSpotDto.setFree(!parkingSpot.getOccupied());

        List<ReservationDto> reservationDtoList = new ArrayList<ReservationDto>();
        parkingSpot.getReservations().forEach(reservation -> {
            ReservationDto reservationDto = new ReservationDto();
            reservationDto.setId(reservation.getId());
            reservationDto.setStart(reservation.getDate());
            reservationDto.setEnd(reservation.getEndDate());
            reservationDto.setPrices(reservation.getParkingCost());
            reservationDto.setCar(reservation.getCar().getId());
            reservationDto.setParkingSpot(reservation.getParkingSpot().getId());
            reservationDtoList.add(reservationDto);
        });
        parkingSpotDto.setReservations(reservationDtoList);
        return  parkingSpotDto;
    }

    private Map<String, List<ParkingSpotDto>> objMapToDtoMap(Map<String, List<Object>> objMap){
        Map<String, List<ParkingSpotDto>> parkingSpots = new HashMap<String, List<ParkingSpotDto>>();
        for (Map.Entry<String,List<Object>> entry : objMap.entrySet()){
//            entry.getKey(), entry.getValue()
            List<ParkingSpotDto> spotsList = new ArrayList<ParkingSpotDto>();
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

        Map<String, List<ParkingSpotDto>> parkingSpots = objMapToDtoMap(parkingSpotsObjectMap);
        List<ParkingSpotDto> parkingSpotDtoList = new ArrayList<ParkingSpotDto>();
        parkingSpots.values().forEach(parkingSpotDtoList::addAll);

        return Response.status(Response.Status.OK).entity(parkingSpotDtoList).build();

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
        List<ParkingSpotDto> parkingSpotsDtoArrayList = new ArrayList<ParkingSpotDto>();

        parkingSpotArrayList.forEach(parkingSpot ->{
            ParkingSpotDto parkingSpotDto = createCarParkingSpotDto(parkingSpot);
            parkingSpotsDtoArrayList.add(parkingSpotDto);
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

        ParkingSpotDto parkingSpotDto = createCarParkingSpotDto((PARKING_SPOT) parkingSpot);

        return Response.status(Response.Status.OK).entity(parkingSpotDto).build();

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
            ParkingSpotDto parkingSpotDto = json.readValue(body, ParkingSpotDto.class);

            if(parkingSpotDto.getType() == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            if(parkingSpotDto.getType().getName() == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            Object carType = carParkService.getCarType(parkingSpotDto.getType().getName());
            CAR_TYPE carTypeCasted;
            Boolean typeCreated;
            if (carType == null){
                carTypeCasted = (CAR_TYPE) carParkService.createCarType(parkingSpotDto.getType().getName());
                typeCreated = Boolean.TRUE;
            }

            else{
                carTypeCasted = (CAR_TYPE) carType;
                typeCreated = Boolean.FALSE;
            }

            Object parkingSpotCreated = carParkService.createParkingSpot(id, identifier, parkingSpotDto.getIdentifier(), carTypeCasted.getId());
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
            ParkingSpotDto parkingSpotDto = json.readValue(body, ParkingSpotDto.class);
            PARKING_SPOT parkingSpot =  new PARKING_SPOT(parkingSpotDto.getIdentifier());
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
