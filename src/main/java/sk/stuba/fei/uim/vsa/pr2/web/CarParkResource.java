package sk.stuba.fei.uim.vsa.pr2.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.stuba.fei.uim.vsa.pr2.domain.CAR_PARK;
import sk.stuba.fei.uim.vsa.pr2.domain.CAR_TYPE;
import sk.stuba.fei.uim.vsa.pr2.domain.USER;
import sk.stuba.fei.uim.vsa.pr2.service.CarParkService;
import sk.stuba.fei.uim.vsa.pr2.web.demand.CarParkDemand;
import sk.stuba.fei.uim.vsa.pr2.web.demand.CarParkFloorDemand;
import sk.stuba.fei.uim.vsa.pr2.web.demand.ParkingSpotDemand;
import sk.stuba.fei.uim.vsa.pr2.web.response.*;
import java.util.*;
import java.util.regex.PatternSyntaxException;


@Path("/")
public class CarParkResource {
    public final CarParkService carParkService = new CarParkService();
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


    private CarParkResponse createCarParkResponse(CAR_PARK carPark){
        CarParkResponse carParkResponse = new CarParkResponse();
        //carpark id do jsonu
        carParkResponse.setId(carPark.getId());
        //carpark adresa do jsonu
        carParkResponse.setAddress(carPark.getAddress());
        //carpark nazov do jsonu
        carParkResponse.setName(carPark.getName());
        //carpark cena za h do jsonu
        carParkResponse.setPrices(carPark.getPricePerHour());
        List<CarParkFloorResponse> carParkFloorResponseList = new ArrayList<CarParkFloorResponse>();

        carPark.getFloors().forEach(floor ->{
            CarParkFloorResponse carParkFloorResponse = new CarParkFloorResponse();
            //floor id do jsonu
            carParkFloorResponse.setIdentifier(floor.getId().getFloorIdentifier());
            carParkFloorResponse.setCarPark(floor.getCarPark().getId());

            List<ParkingSpotResponse> parkingSpotResponseList = new ArrayList<ParkingSpotResponse>();
            floor.getParkingSpots().forEach(parkingSpot -> {
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
            carParkFloorResponseList.add(carParkFloorResponse);
        });
        carParkResponse.setFloors(carParkFloorResponseList);
        return carParkResponse;
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

            CarParkResponse carParkResponse = createCarParkResponse((CAR_PARK) carPark);
            List<CarParkResponse> carParkResponseList = new ArrayList<CarParkResponse>();
            carParkResponseList.add(carParkResponse);
            return Response.status(Response.Status.OK).entity(carParkResponseList).build();
        }

        List<CAR_PARK> carParks = new ArrayList<CAR_PARK>();
        carParkService.getCarParks().forEach(carPark -> carParks.add((CAR_PARK) carPark));
        List<CarParkResponse> carParkResponseList = new ArrayList<CarParkResponse>();

        carParks.forEach(carPark ->{
            CarParkResponse carParkResponse = createCarParkResponse(carPark);
            carParkResponseList.add(carParkResponse);
        });
        return Response.status(Response.Status.OK).entity(carParkResponseList).build();

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

        CarParkResponse carParkResponse = createCarParkResponse((CAR_PARK) carPark);

        return Response.status(Response.Status.OK).entity(carParkResponse).build();

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
            CarParkDemand carParkDemand = json.readValue(body, CarParkDemand.class);
            Object carParkCreated = carParkService.createCarPark(carParkDemand.getName(), carParkDemand.getAddress(), carParkDemand.getPrices());

            if (carParkCreated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            CAR_PARK carParkCreatedCast = (CAR_PARK) carParkCreated;


            if (carParkDemand.getFloors() != null)
                for (CarParkFloorDemand floor : carParkDemand.getFloors()){
                    Object floorCreated = carParkService.createCarParkFloor(carParkCreatedCast.getId(), floor.getIdentifier());
                    if (floorCreated == null){
                        carParkService.deleteCarPark((carParkCreatedCast.getId()));
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                    if (floor.getSpots() != null)
                        for (ParkingSpotDemand parkingSpot :floor.getSpots()){

                            Object carType;
                            if(parkingSpot.getType().getId() == null && parkingSpot.getType().getName() != null)
                                carType = carParkService.getCarType(parkingSpot.getType().getName());

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

                            Object parkingSpotCreated = carParkService.createParkingSpot(carParkCreatedCast.getId(), parkingSpot.getCarParkFloor(),parkingSpot.getIdentifier(), carTypeCasted.getId());
                            if (parkingSpotCreated == null){
                                if (typeCreated)
                                    carParkService.deleteCarType(carTypeCasted.getId());

                                carParkService.deleteCarPark((carParkCreatedCast.getId()));
                                return Response.status(Response.Status.BAD_REQUEST).build();
                            }
                        }
                }


            CAR_PARK carParkCreatedWhole = (CAR_PARK) carParkService.getCarPark(carParkCreatedCast.getId());
            return Response.status(Response.Status.CREATED).entity(createCarParkResponse(carParkCreatedWhole)).build();

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
            CarParkResponse carParkResponse = json.readValue(body, CarParkResponse.class);
            CAR_PARK carPark =  new CAR_PARK(carParkResponse.getAddress(), carParkResponse.getName(), carParkResponse.getPrices());
            carPark.setId(id);
            Object carParkUpdated = carParkService.updateCarPark(carPark);
            if (carParkUpdated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            return Response.status(Response.Status.OK).entity(createCarParkResponse((CAR_PARK) carParkUpdated)).build();

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
