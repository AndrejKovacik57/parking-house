package sk.stuba.fei.uim.vsa.pr2.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.stuba.fei.uim.vsa.pr2.domain.CAR_PARK;
import sk.stuba.fei.uim.vsa.pr2.domain.CAR_TYPE;
import sk.stuba.fei.uim.vsa.pr2.service.CarParkService;
import sk.stuba.fei.uim.vsa.pr2.web.response.CarParkDto;
import sk.stuba.fei.uim.vsa.pr2.web.response.CarTypeDto;
import sk.stuba.fei.uim.vsa.pr2.web.response.UserDto;

import java.util.ArrayList;
import java.util.List;

@Path("/")
public class CarTypeResource {
    private final CarParkService carParkService = new CarParkService();
    private final ObjectMapper json = new ObjectMapper();

    @GET
    @Path("/cartypes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCarTypes(@QueryParam("name") String name){
        if(name != null){
            Object carType = carParkService.getCarType(name);
            if (carType == null)
                return Response.status(Response.Status.NOT_FOUND).build();
            CAR_TYPE carTypeCasted = (CAR_TYPE) carType;

            CarTypeDto carTypeDto = new CarTypeDto(carTypeCasted.getId(),carTypeCasted.getName());
            List<CarTypeDto> carTypeDtoList = new ArrayList<CarTypeDto>();
            carTypeDtoList.add(carTypeDto);
            return Response.status(Response.Status.OK).entity(carTypeDtoList).build();
        }

        List<CAR_TYPE> carTypeList = new ArrayList<CAR_TYPE>();
        carParkService.getCarTypes().forEach(carType -> carTypeList.add((CAR_TYPE) carType));

        List<CarTypeDto> carTypeDtoList = new ArrayList<CarTypeDto>();

        carTypeList.forEach(carType -> carTypeDtoList.add(new CarTypeDto(carType.getId(),carType.getName())));

        return Response.status(Response.Status.OK).entity(carTypeDtoList).build();

    }

    @GET
    @Path("/cartypes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCarTypes(@PathParam("id") Long id){

        Object carType = carParkService.getCarType(id);
        if (carType == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        CAR_TYPE carTypeCasted = (CAR_TYPE) carType;

        CarTypeDto carTypeDto = new CarTypeDto(carTypeCasted.getId(),carTypeCasted.getName());

        return Response.status(Response.Status.OK).entity(carTypeDto).build();

    }
    @POST
    @Path("/cartypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCarType(String body){
        try{
            CAR_TYPE carType = json.readValue(body, CAR_TYPE.class);
            Object carTypeCreated = carParkService.createCarType(carType.getName());
            if (carTypeCreated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();
            CAR_TYPE carTypeCasted = (CAR_TYPE) carTypeCreated;

            CarTypeDto carTypeDto = new CarTypeDto(carTypeCasted.getId(),carTypeCasted.getName());
            return Response.status(Response.Status.OK).entity(carTypeDto).build();

        }catch(JsonProcessingException e){
            System.err.println(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

    }

    @DELETE
    @Path("/cartypes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("id") Long id){
        Object carTypeDeleted = carParkService.deleteCarType(id);
        if (carTypeDeleted == null)
            return Response.status(Response.Status.BAD_REQUEST).build();

        return Response.status(Response.Status.NO_CONTENT).build();

    }
}
