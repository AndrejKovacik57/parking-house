package sk.stuba.fei.uim.vsa.pr2.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.stuba.fei.uim.vsa.pr2.domain.CAR_TYPE;
import sk.stuba.fei.uim.vsa.pr2.domain.USER;
import sk.stuba.fei.uim.vsa.pr2.service.CarParkService;
import sk.stuba.fei.uim.vsa.pr2.web.demand.CarDemand;
import sk.stuba.fei.uim.vsa.pr2.web.demand.UserDemand;
import sk.stuba.fei.uim.vsa.pr2.web.response.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Path("/")
public class UserResource {
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


    private UserResponse createUserResponse(USER user){
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setEmail(user.getEmail());

        List<CarResponse> carResponseList = new ArrayList<CarResponse>();
        user.getCars().forEach(car -> {
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
            carResponseList.add(carResponse);
        });
        userResponse.setCars(carResponseList);
        return userResponse;
    }

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @QueryParam("email") String email){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        if(email != null){
            Object user = carParkService.getUser(email);
            if (user == null)
                return Response.status(Response.Status.NOT_FOUND).build();

            UserResponse userResponse = createUserResponse((USER) user);
            List<UserResponse> userResponses = new ArrayList<UserResponse>();
            userResponses.add(userResponse);
            return Response.status(Response.Status.OK).entity(userResponses).build();
        }
        List<USER> userList = new ArrayList<USER>();
        carParkService.getUsers().forEach(user -> {
            userList.add((USER) user);
        });

        List<UserResponse> userResponses = new ArrayList<UserResponse>();

        userList.forEach(user ->{
            UserResponse userResponse = createUserResponse(user);
            userResponses.add(userResponse);
        });
        return Response.status(Response.Status.OK).entity(userResponses).build();
    }

    @GET
    @Path("/users/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Object user = carParkService.getUser(id);
        if (user == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        UserResponse userResponse = createUserResponse((USER) user);

        return Response.status(Response.Status.OK).entity(userResponse).build();

    }
    @POST
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, String body){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        try{
            UserDemand userDemand = json.readValue(body, UserDemand.class);
            Object userCreated = carParkService.createUser(userDemand.getFirstName(), userDemand.getLastName(), userDemand.getEmail());

            if (userCreated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            USER userCreatedCast = (USER) userCreated;
            if(userDemand.getCars() != null)
                for(CarDemand carDemand : userDemand.getCars()){

                    if(carDemand.getType() == null){
                        carParkService.deleteUser(userCreatedCast.getId());
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                    if(carDemand.getType().getName() == null){
                        carParkService.deleteUser(userCreatedCast.getId());
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }

                    Object carType = carParkService.getCarType(carDemand.getType().getName());
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

                    Object carCreated = carParkService.createCar(userCreatedCast.getId(), carDemand.getBrand(), carDemand.getModel(), carDemand.getColour(), carDemand.getVrp(),carTypeCasted.getId());
                    if(carCreated == null){
                        if (typeCreated)
                            carParkService.deleteCarType(carTypeCasted.getId());
                        carParkService.deleteUser(userCreatedCast.getId());
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }

                }

            USER UserCreatedWhole = (USER) carParkService.getUser(userCreatedCast.getId());
            return Response.status(Response.Status.CREATED).entity(createUserResponse(UserCreatedWhole)).build();

        }catch(JsonProcessingException e){
            System.err.println(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

    }
    @PUT
    @Path("/users/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id, String body){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        try{
            UserResponse userDemand = json.readValue(body, UserResponse.class);
            USER user =  new USER(userDemand.getFirstName(), userDemand.getLastName(), userDemand.getEmail());
            user.setId(id);
            Object userUpdated = carParkService.updateUser(user);
            if (userUpdated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            return Response.status(Response.Status.OK).entity(createUserResponse((USER) userUpdated)).build();

        }catch (JsonProcessingException e){
            System.err.println(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @DELETE
    @Path("/users/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Object userDeleted = carParkService.deleteUser(id);
        if (userDeleted == null)
            return Response.status(Response.Status.BAD_REQUEST).build();

        return Response.status(Response.Status.NO_CONTENT).build();

    }
}
