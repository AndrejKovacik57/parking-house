package sk.stuba.fei.uim.vsa.pr2.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.stuba.fei.uim.vsa.pr2.domain.CAR;
import sk.stuba.fei.uim.vsa.pr2.domain.CAR_TYPE;
import sk.stuba.fei.uim.vsa.pr2.domain.USER;
import sk.stuba.fei.uim.vsa.pr2.service.CarParkService;
import sk.stuba.fei.uim.vsa.pr2.web.demand.CarDemand;
import sk.stuba.fei.uim.vsa.pr2.web.demand.CarWithOwnerDemand;
import sk.stuba.fei.uim.vsa.pr2.web.response.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.PatternSyntaxException;

@Path("/")
public class CarResource {
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


    private CarResponse createCarResponse(CAR car){
        CarResponse carResponse = new CarResponse();
        carResponse.setId(car.getId());
        carResponse.setVrp(car.getVehicleRegistrationPlate());
        carResponse.setBrand(car.getBrand());
        carResponse.setModel(car.getModel());
        carResponse.setColour(car.getColour());
        carResponse.setOwner(car.getUser().getId());

        carResponse.setType(new CarTypeResponse(car.getCarType().getId(), car.getCarType().getName()));
        List<ReservationResponse> reservationResponseList = new ArrayList<ReservationResponse>();
        car.getReservations().forEach(reservation -> {
            ReservationResponse reservationResponse = new ReservationResponse();
            reservationResponse.setId(reservation.getId());
            reservationResponse.setStart(reservation.getDate());
            reservationResponse.setEnd(reservation.getEndDate());
            reservationResponse.setPrices(reservation.getParkingCost());
            reservationResponse.setCar(reservation.getCar().getId());
            reservationResponse.setParkingSpot(reservation.getParkingSpot().getId());
            reservationResponseList.add(reservationResponse);
        });
        carResponse.setReservations(reservationResponseList);

        return carResponse;
    }
    private List<CarResponse> getCarsByUser(Long user){
        List<Object> cars = carParkService.getCars(user);
        if (cars == null)
            return new ArrayList<CarResponse>();

        List<CAR> carsCasted = new ArrayList<CAR>();
        cars.forEach(car -> carsCasted.add((CAR) car));

        List<CarResponse> carResponseList = new ArrayList<CarResponse>();

        carsCasted.forEach(car -> carResponseList.add(createCarResponse(car)));
        return carResponseList;
    }
    private List<CarResponse> getCarsByVrp(String vrp){
        Object car = carParkService.getCar(vrp);
        if (car == null)
            return new ArrayList<CarResponse>();

        CarResponse carResponse = createCarResponse((CAR) car);
        List<CarResponse> carResponseList = new ArrayList<CarResponse>();
        carResponseList.add(carResponse);
        return carResponseList;
    }
    @GET
    @Path("/cars")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCars(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @QueryParam("user") Long user, @QueryParam("vrp") String vrp){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        if(user != null && vrp == null){
            List<CarResponse> carResponseList = getCarsByUser(user);
            return Response.status(Response.Status.OK).entity(carResponseList).build();
        }
        else if(vrp != null && user == null){
            List<CarResponse> carResponseList = getCarsByVrp(vrp);
            return Response.status(Response.Status.OK).entity(carResponseList).build();
        }
        else if(user != null){
            List<CarResponse> carResponseListVrp = getCarsByVrp(vrp);
            List<CarResponse> carResponseListUser = getCarsByUser(user);
            List<CarResponse> intersectionList = new ArrayList<CarResponse>();
            carResponseListUser.forEach(userCar -> carResponseListVrp.forEach(vrpCar -> {
                if(userCar.getId().equals(vrpCar.getId()))
                    intersectionList.add(vrpCar);
            }));
            return Response.status(Response.Status.OK).entity(intersectionList).build();
        }
        else{
            List<CAR> cars = new ArrayList<CAR>();
            carParkService.getCars().forEach(car -> cars.add((CAR) car));
            List<CarResponse> carResponseList = new ArrayList<CarResponse>();

            cars.forEach(car ->{
                CarResponse carResponse = createCarResponse(car);
                carResponseList.add(carResponse);
            });
            return Response.status(Response.Status.OK).entity(carResponseList).build();
        }
    }
    @GET
    @Path("/cars/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCars(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Object car = carParkService.getCar(id);
        if (car == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        CarResponse carResponse = createCarResponse((CAR) car);

        return Response.status(Response.Status.OK).entity(carResponse).build();

    }
    @POST
    @Path("/cars")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCar(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, String body){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        try{
            CarWithOwnerDemand carDemand = json.readValue(body, CarWithOwnerDemand.class);

            if(carDemand.getType() == null )
                return Response.status(Response.Status.BAD_REQUEST).build();

            Object carType;
            if(carDemand.getType().getId() == null && carDemand.getType().getName() != null){
                carType = carParkService.getCarType(carDemand.getType().getName());

            }

            else if(carDemand.getType().getName() == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            else
                carType = carParkService.getCarType(carDemand.getType().getId());

            Object user;
            if (carDemand.getOwner().getId() == null)
                user = carParkService.getUser(carDemand.getOwner().getEmail());
            else
                user = carParkService.getUser(carDemand.getOwner().getId());

            USER userCast;
            Boolean userCreatedBool;
            if (user == null){
                Object userCreated = carParkService.createUser(carDemand.getOwner().getFirstName(),carDemand.getOwner().getLastName(),carDemand.getOwner().getEmail());
                if (userCreated == null)
                    return Response.status(Response.Status.BAD_REQUEST).build();
                else{
                    userCast = (USER) userCreated;
                    userCreatedBool = Boolean.TRUE;
                }
            }
            else{
                userCast = (USER) user;
//                if (!userCast.getFirstName().equals(carDemand.getOwner().getFirstName()) || !userCast.getLastName().equals(carDemand.getOwner().getLastName()))
//                    return Response.status(Response.Status.BAD_REQUEST).build();
                userCreatedBool = Boolean.FALSE;
            }


            CAR_TYPE carTypeCasted;
            Boolean typeCreated;
            if (carType == null){
                carTypeCasted = (CAR_TYPE) carParkService.createCarType(carDemand.getType().getName());
                typeCreated = Boolean.TRUE;
            }

            else{
                carTypeCasted = (CAR_TYPE) carType;
                typeCreated = Boolean.FALSE;
            }

            Object carCreated = carParkService.createCar(userCast.getId(),carDemand.getBrand(),carDemand.getModel(),carDemand.getColour(),carDemand.getVrp(),carTypeCasted.getId());
            if(carCreated == null){
                if (typeCreated)
                    carParkService.deleteCarType(carTypeCasted.getId());
                if (userCreatedBool)
                    carParkService.deleteUser(userCast.getId());
                return Response.status(Response.Status.BAD_REQUEST).build();
            }


            CAR carCreatedCast = (CAR) carCreated;

            System.err.println(carCreated);
            return Response.status(Response.Status.CREATED).entity(createCarResponse(carCreatedCast)).build();

        }catch(JsonProcessingException e){
            System.err.println(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    @PUT
    @Path("/cars/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCar(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id, String body){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        try{
            CarResponse carResponse = json.readValue(body, CarResponse.class);
            CAR car =  new CAR(carResponse.getVrp(), carResponse.getBrand(), carResponse.getModel(), carResponse.getColour());
            car.setId(id);
            Object carUpdated = carParkService.updateCar(car);
            if (carUpdated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            return Response.status(Response.Status.OK).entity(createCarResponse((CAR) carUpdated)).build();

        }catch (JsonProcessingException e){
            System.err.println(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    @DELETE
    @Path("/cars/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCar(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Object carDeleted = carParkService.deleteCar(id);
        if (carDeleted == null)
            return Response.status(Response.Status.BAD_REQUEST).build();

        return Response.status(Response.Status.NO_CONTENT).build();

    }
}

