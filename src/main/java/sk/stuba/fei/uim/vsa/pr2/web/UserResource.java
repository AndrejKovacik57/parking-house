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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Path("/")
public class UserResource {
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

    private UserDto createUserDtio(USER user){
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());

        List<CarDto> carDtoList = new ArrayList<CarDto>();
        user.getCars().forEach(car -> {
            CarDto carDto = new CarDto();
            carDto.setId(car.getId());
            carDto.setVrp(car.getVehicleRegistrationPlate());
            carDto.setBrand(car.getBrand());
            carDto.setModel(car.getModel());
            carDto.setColour(car.getColour());
            carDto.setOwner(car.getUser().getId());
            carDto.setType(new CarTypeDto(car.getCarType().getId(), car.getCarType().getName()));
            List<ReservationDto> reservationDtoList = new ArrayList<ReservationDto>();
            car.getReservations().forEach(reservation -> {
                ReservationDto reservationDto = new ReservationDto();
                reservationDto.setId(reservation.getId());
                reservationDto.setStart(reservation.getDate());
                reservationDto.setEnd(reservation.getEndDate());
                reservationDto.setPrices(reservation.getParkingCost());
                reservationDto.setCar(reservation.getCar().getId());
                reservationDto.setParkingSpot(reservation.getParkingSpot().getId());
                reservationDtoList.add(reservationDto);
            });
            carDto.setReservations(reservationDtoList);
            carDtoList.add(carDto);
        });
        userDto.setCars(carDtoList);
        return  userDto;
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

            UserDto userDto = createUserDtio((USER) user);
            List<UserDto> userDtos = new ArrayList<UserDto>();
            userDtos.add(userDto);
            return Response.status(Response.Status.OK).entity(userDtos).build();
        }
        List<USER> userList = new ArrayList<USER>();
        carParkService.getUsers().forEach(user -> {
            userList.add((USER) user);
        });

        List<UserDto> userDtos = new ArrayList<UserDto>();

        userList.forEach(user ->{
            UserDto userDto = createUserDtio(user);
            userDtos.add(userDto);
        });
        return Response.status(Response.Status.OK).entity(userDtos).build();
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

        UserDto userDto = createUserDtio((USER) user);

        return Response.status(Response.Status.OK).entity(userDto).build();

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
            UserDto userDto = json.readValue(body, UserDto.class);
            Object userCreated = carParkService.createUser(userDto.getFirstName(),userDto.getLastName(),userDto.getEmail());

            if (userCreated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            USER userCreatedCast = (USER) userCreated;
            try {
                for(CarDto carDto : userDto.getCars()){

                    if(carDto.getType() == null){
                        carParkService.deleteUser(userCreatedCast.getId());
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                    if(carDto.getType().getName() == null){
                        carParkService.deleteUser(userCreatedCast.getId());
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }

                    Object carType = carParkService.getCarType(carDto.getType().getName());
                    CAR_TYPE carTypeCasted;
                    Boolean typeCreated;
                    if (carType == null){
                        carTypeCasted = (CAR_TYPE) carParkService.createCarType(carDto.getType().getName());
                        typeCreated = Boolean.TRUE;
                    }

                    else{
                        carTypeCasted = (CAR_TYPE) carType;
                        typeCreated = Boolean.FALSE;
                    }

                    Object carCreated = carParkService.createCar(userCreatedCast.getId(), carDto.getBrand(),carDto.getModel(),carDto.getColour(),carDto.getVrp(),carTypeCasted.getId());
                    if(carCreated == null){
                        if (typeCreated)
                            carParkService.deleteCarType(carTypeCasted.getId());
                        carParkService.deleteUser(userCreatedCast.getId());
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }

                }
            }catch (NotInitializedException ignore){}

            USER UserCreatedWhole = (USER) carParkService.getUser(userCreatedCast.getId());
            return Response.status(Response.Status.CREATED).entity(createUserDtio(UserCreatedWhole)).build();

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
            UserDto userDto = json.readValue(body, UserDto.class);
            USER user =  new USER(userDto.getFirstName(),userDto.getLastName(),userDto.getEmail());
            user.setId(id);
            Object userUpdated = carParkService.updateUser(user);
            if (userUpdated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            return Response.status(Response.Status.OK).entity(createUserDtio((USER) userUpdated)).build();

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
