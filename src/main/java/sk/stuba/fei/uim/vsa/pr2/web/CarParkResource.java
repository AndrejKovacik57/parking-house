package sk.stuba.fei.uim.vsa.pr2.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.stuba.fei.uim.vsa.pr2.domain.CAR_PARK;
import sk.stuba.fei.uim.vsa.pr2.domain.CAR_TYPE;
import sk.stuba.fei.uim.vsa.pr2.domain.USER;
import sk.stuba.fei.uim.vsa.pr2.exception.NotInitializedException;
import sk.stuba.fei.uim.vsa.pr2.service.CarParkService;
import sk.stuba.fei.uim.vsa.pr2.web.response.*;
import java.util.*;


@Path("/")
public class CarParkResource {
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

    private CarParkDto createCarParkDto(CAR_PARK carPark){
        CarParkDto carParkDto = new CarParkDto();
        //carpark id do jsonu
        carParkDto.setId(carPark.getId());
        //carpark adresa do jsonu
        carParkDto.setAddress(carPark.getAddress());
        //carpark nazov do jsonu
        carParkDto.setName(carPark.getName());
        //carpark cena za h do jsonu
        carParkDto.setPrices(carPark.getPricePerHour());
        List<CarParkFloorDto> carParkFloorDtoList = new ArrayList<CarParkFloorDto>();

        carPark.getFloors().forEach(floor ->{
            CarParkFloorDto carParkFloorDto = new CarParkFloorDto();
            //floor id do jsonu
            carParkFloorDto.setIdentifier(floor.getId().getFloorIdentifier());
            carParkFloorDto.setCarPark(floor.getCarPark().getId());

            List<ParkingSpotDto> parkingSpotDtoList = new ArrayList<ParkingSpotDto>();
            floor.getParkingSpots().forEach(parkingSpot -> {
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
            carParkFloorDtoList.add(carParkFloorDto);
        });
        carParkDto.setFloors(carParkFloorDtoList);
        return  carParkDto;
    }

    @GET
    @Path("/carparks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCarParks(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @QueryParam("name") String name){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        if(name != null){
            Object carPark = carParkService.getCarPark(name);
            if (carPark == null)
                return Response.status(Response.Status.NOT_FOUND).build();

            CarParkDto carParkDto = createCarParkDto((CAR_PARK) carPark);
            List<CarParkDto> carParkDtoList = new ArrayList<CarParkDto>();
            carParkDtoList.add(carParkDto);
            return Response.status(Response.Status.OK).entity(carParkDtoList).build();
        }

        List<CAR_PARK> carParks = new ArrayList<CAR_PARK>();
        carParkService.getCarParks().forEach(carPark -> carParks.add((CAR_PARK) carPark));
        List<CarParkDto> carParkDtoList = new ArrayList<CarParkDto>();

        carParks.forEach(carPark ->{
            CarParkDto carParkDto = createCarParkDto(carPark);
            carParkDtoList.add(carParkDto);
        });
        return Response.status(Response.Status.OK).entity(carParkDtoList).build();

    }


    @GET
    @Path("/carparks/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCarParks(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Object carPark = carParkService.getCarPark(id);
        if (carPark == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        CarParkDto carParkDto = createCarParkDto((CAR_PARK) carPark);

        return Response.status(Response.Status.OK).entity(carParkDto).build();

    }


    @POST
    @Path("/carparks")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCarPark(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, String body){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        try{
            CarParkDto carParkDto = json.readValue(body, CarParkDto.class);
            Object carParkCreated = carParkService.createCarPark(carParkDto.getName(), carParkDto.getAddress(), carParkDto.getPrices());

            if (carParkCreated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            CAR_PARK carParkCreatedCast = (CAR_PARK) carParkCreated;

            try {
                for (CarParkFloorDto floor :carParkDto.getFloors()){
                    Object floorCreated = carParkService.createCarParkFloor(carParkCreatedCast.getId(), floor.getIdentifier());
                    if (floorCreated == null){
                        carParkService.deleteCarPark((carParkCreatedCast.getId()));
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }

                    for (ParkingSpotDto parkingSpot :floor.getSpots()){
                        if(parkingSpot.getType() == null){
                            carParkService.deleteCarPark((carParkCreatedCast.getId()));
                            return Response.status(Response.Status.BAD_REQUEST).build();
                        }
                        if(parkingSpot.getType().getName() == null){
                            carParkService.deleteCarPark((carParkCreatedCast.getId()));
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

                        Object parkingSpotCreated = carParkService.createParkingSpot(carParkCreatedCast.getId(), parkingSpot.getCarParkFloor(),parkingSpot.getIdentifier(), carTypeCasted.getId());
                        if (parkingSpotCreated == null){
                            if (typeCreated)
                                carParkService.deleteCarType(carTypeCasted.getId());

                            carParkService.deleteCarPark((carParkCreatedCast.getId()));
                            return Response.status(Response.Status.BAD_REQUEST).build();
                        }
                    }
                }
            }catch (NotInitializedException ignore){}

            CAR_PARK carParkCreatedWhole = (CAR_PARK) carParkService.getCarPark(carParkCreatedCast.getId());
            return Response.status(Response.Status.CREATED).entity(createCarParkDto(carParkCreatedWhole)).build();

        }catch (JsonProcessingException e){
            System.err.println(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    @PUT
    @Path("/carparks/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCarPark(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id, String body){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        try{
            CarParkDto carParkDto = json.readValue(body, CarParkDto.class);
            CAR_PARK carPark =  new CAR_PARK(carParkDto.getAddress(), carParkDto.getName(), carParkDto.getPrices());
            carPark.setId(id);
            Object carParkUpdated = carParkService.updateCarPark(carPark);
            if (carParkUpdated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            return Response.status(Response.Status.OK).entity(createCarParkDto((CAR_PARK) carParkUpdated)).build();

        }catch (JsonProcessingException e){
            System.err.println(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @DELETE
    @Path("/carparks/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCarParks(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Object carparkDeleted = carParkService.deleteCarPark(id);
        if (carparkDeleted == null)
            return Response.status(Response.Status.BAD_REQUEST).build();

        return Response.status(Response.Status.NO_CONTENT ).build();

    }

}
