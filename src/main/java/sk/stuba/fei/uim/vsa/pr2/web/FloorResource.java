package sk.stuba.fei.uim.vsa.pr2.web;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.stuba.fei.uim.vsa.pr2.domain.*;
import sk.stuba.fei.uim.vsa.pr2.exception.NotInitializedException;
import sk.stuba.fei.uim.vsa.pr2.web.response.*;
import sk.stuba.fei.uim.vsa.pr2.service.CarParkService;

import java.util.*;


@Path("/")
public class FloorResource {
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

    private CarParkFloorDto createCarParkFloorDto(CAR_PARK_FLOOR carParkFloor){
        CarParkFloorDto carParkFloorDto = new CarParkFloorDto();
        carParkFloorDto.setIdentifier(carParkFloor.getId().getFloorIdentifier());
        carParkFloorDto.setCarPark(carParkFloor.getCarPark().getId());

        List<ParkingSpotDto> parkingSpotDtoList = new ArrayList<ParkingSpotDto>();

        carParkFloor.getParkingSpots().forEach(parkingSpot -> {
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
            parkingSpotDtoList.add(parkingSpotDto);
        });
        carParkFloorDto.setSpots(parkingSpotDtoList);

        return  carParkFloorDto;
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
        List<CarParkFloorDto> CarParkFloorDtoArrayList = new ArrayList<CarParkFloorDto>();

        carParkFloors.forEach(floor ->{
            CarParkFloorDto carParkFloorDto = createCarParkFloorDto(floor);
            CarParkFloorDtoArrayList.add(carParkFloorDto);
        });

        return Response.status(Response.Status.OK).entity(CarParkFloorDtoArrayList).build();

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

        CarParkFloorDto carParkFloorDto = createCarParkFloorDto((CAR_PARK_FLOOR) carParkFloor);

        return Response.status(Response.Status.OK).entity(carParkFloorDto).build();

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
            CarParkFloorDto carParkDto = json.readValue(body, CarParkFloorDto.class);


            Object carParkFloorCreated = carParkService.createCarParkFloor(id,carParkDto.getIdentifier());

            if (carParkFloorCreated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            CAR_PARK_FLOOR carParkFloorCreatedCast = (CAR_PARK_FLOOR) carParkFloorCreated;

            try {
                for (ParkingSpotDto parkingSpot :carParkDto.getSpots()){
                    if(parkingSpot.getType() == null){
                        carParkService.deleteCarParkFloor(id,carParkDto.getIdentifier());
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                    if(parkingSpot.getType().getName() == null){
                        carParkService.deleteCarParkFloor(id,carParkDto.getIdentifier());
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }

                    Object carType = carParkService.getCarType(parkingSpot.getType().getName());
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

                        carParkService.deleteCarParkFloor(carParkDto.getCarPark(),carParkDto.getIdentifier());
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                }
            }catch (NotInitializedException ignore){}


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
