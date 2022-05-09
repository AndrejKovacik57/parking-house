package sk.stuba.fei.uim.vsa.pr2.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.stuba.fei.uim.vsa.pr2.domain.CAR;
import sk.stuba.fei.uim.vsa.pr2.domain.CAR_TYPE;
import sk.stuba.fei.uim.vsa.pr2.domain.USER;
import sk.stuba.fei.uim.vsa.pr2.service.CarParkService;
import sk.stuba.fei.uim.vsa.pr2.web.response.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Path("/")
public class CarResource {
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

    private CarDto createCarDto(CAR car){
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

        return carDto;
    }
    private List<CarDto> getCarsByUser(Long user){
        List<Object> cars = carParkService.getCars(user);
        if (cars == null)
            return new ArrayList<CarDto>();

        List<CAR> carsCasted = new ArrayList<CAR>();
        cars.forEach(car -> carsCasted.add((CAR) car));

        List<CarDto> carDtoList = new ArrayList<CarDto>();

        carsCasted.forEach(car -> carDtoList.add(createCarDto(car)));
        return carDtoList;
    }
    private List<CarDto> getCarsByVrp(String vrp){
        Object car = carParkService.getCar(vrp);
        if (car == null)
            return new ArrayList<CarDto>();

        CarDto carDto = createCarDto((CAR) car);
        List<CarDto> carDtoList = new ArrayList<CarDto>();
        carDtoList.add(carDto);
        return carDtoList;
    }
    @GET
    @Path("/cars")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCars(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @QueryParam("user") Long user, @QueryParam("vrp") String vrp){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        if(user != null && vrp == null){
            List<CarDto> carDtoList = getCarsByUser(user);
            if (carDtoList.isEmpty())
                return Response.status(Response.Status.NOT_FOUND).build();

            return Response.status(Response.Status.OK).entity(carDtoList).build();
        }
        else if(vrp != null && user == null){
            List<CarDto> carDtoList = getCarsByVrp(vrp);
            if (carDtoList.isEmpty())
                return Response.status(Response.Status.NOT_FOUND).build();

            return Response.status(Response.Status.OK).entity(carDtoList).build();
        }
        else if(user != null){
            List<CarDto> carDtoListVrp = getCarsByVrp(vrp);
            List<CarDto> carDtoListUser = getCarsByUser(user);
            List<CarDto> intersectionList = new ArrayList<CarDto>();
            carDtoListUser.forEach(userCar -> carDtoListVrp.forEach(vrpCar -> {
                if(userCar.getId().equals(vrpCar.getId()))
                    intersectionList.add(vrpCar);
            }));
            return Response.status(Response.Status.OK).entity(intersectionList).build();
        }
        else{
            List<CAR> cars = new ArrayList<CAR>();
            carParkService.getCars().forEach(car -> cars.add((CAR) car));
            List<CarDto> carDtoList = new ArrayList<CarDto>();

            cars.forEach(car ->{
                CarDto carDto = createCarDto(car);
                carDtoList.add(carDto);
            });
            return Response.status(Response.Status.OK).entity(carDtoList).build();
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

        CarDto carDto = createCarDto((CAR) car);

        return Response.status(Response.Status.OK).entity(carDto).build();

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
            CarDtoWithUsers carDto = json.readValue(body, CarDtoWithUsers.class);

            if(carDto.getType() == null){
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if(carDto.getType().getName() == null){
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            Object user = carParkService.getUser(carDto.getOwner().getId());
            USER userCast;
            Boolean userCreatedBool;
            if (user == null){
                Object userCreated = carParkService.createUser(carDto.getOwner().getFirstName(),carDto.getOwner().getLastName(),carDto.getOwner().getEmail());
                if (userCreated == null)
                    return Response.status(Response.Status.BAD_REQUEST).build();
                else{
                    userCast = (USER) userCreated;
                    userCreatedBool = Boolean.TRUE;
                }

            }
            else{
                userCast = (USER) user;
                userCreatedBool = Boolean.FALSE;
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

            Object carCreated = carParkService.createCar(userCast.getId(),carDto.getBrand(),carDto.getModel(),carDto.getColour(),carDto.getVrp(),carTypeCasted.getId());
            if(carCreated == null){
                if (typeCreated)
                    carParkService.deleteCarType(carTypeCasted.getId());
                if (userCreatedBool)
                    carParkService.deleteUser(userCast.getId());
                return Response.status(Response.Status.BAD_REQUEST).build();
            }


            CAR carCreatedCast = (CAR) carCreated;
            CAR carCreatedWhole = (CAR) carParkService.getUser(carCreatedCast.getId());
            return Response.status(Response.Status.CREATED).entity(createCarDto(carCreatedWhole)).build();

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
            CarDto carDto = json.readValue(body, CarDto.class);
            CAR car =  new CAR(carDto.getVrp(),carDto.getBrand(),carDto.getModel(),carDto.getColour());
            car.setId(id);
            Object carUpdated = carParkService.updateCar(car);
            if (carUpdated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();

            return Response.status(Response.Status.OK).entity(createCarDto((CAR) carUpdated)).build();

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

