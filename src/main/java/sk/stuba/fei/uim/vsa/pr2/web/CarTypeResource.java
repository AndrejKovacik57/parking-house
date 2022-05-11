package sk.stuba.fei.uim.vsa.pr2.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.stuba.fei.uim.vsa.pr2.domain.CAR_TYPE;
import sk.stuba.fei.uim.vsa.pr2.domain.USER;
import sk.stuba.fei.uim.vsa.pr2.service.CarParkService;
import sk.stuba.fei.uim.vsa.pr2.web.response.CarTypeResponse;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.PatternSyntaxException;

@Path("/")
public class CarTypeResource {
    private final CarParkService carParkService = new CarParkService();
    private final ObjectMapper json = new ObjectMapper();

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


    @GET
    @Path("/cartypes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCarTypes(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @QueryParam("name") String name){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        if(name != null){
            Object carType = carParkService.getCarType(name);
            if (carType == null)
                return Response.status(Response.Status.NOT_FOUND).build();
            CAR_TYPE carTypeCasted = (CAR_TYPE) carType;

            CarTypeResponse carTypeDto = new CarTypeResponse(carTypeCasted.getId(),carTypeCasted.getName());
            List<CarTypeResponse> carTypeDtoList = new ArrayList<CarTypeResponse>();
            carTypeDtoList.add(carTypeDto);
            return Response.status(Response.Status.OK).entity(carTypeDtoList).build();
        }

        List<CAR_TYPE> carTypeList = new ArrayList<CAR_TYPE>();
        carParkService.getCarTypes().forEach(carType -> carTypeList.add((CAR_TYPE) carType));

        List<CarTypeResponse> carTypeDtoList = new ArrayList<CarTypeResponse>();

        carTypeList.forEach(carType -> carTypeDtoList.add(new CarTypeResponse(carType.getId(),carType.getName())));

        return Response.status(Response.Status.OK).entity(carTypeDtoList).build();

    }

    @GET
    @Path("/cartypes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCarTypes(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Object carType = carParkService.getCarType(id);
        if (carType == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        CAR_TYPE carTypeCasted = (CAR_TYPE) carType;

        CarTypeResponse carTypeDto = new CarTypeResponse(carTypeCasted.getId(),carTypeCasted.getName());

        return Response.status(Response.Status.OK).entity(carTypeDto).build();

    }
    @POST
    @Path("/cartypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCarType(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, String body){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        try{
            CAR_TYPE carType = json.readValue(body, CAR_TYPE.class);
            Object carTypeCreated = carParkService.createCarType(carType.getName());
            if (carTypeCreated == null)
                return Response.status(Response.Status.BAD_REQUEST).build();
            CAR_TYPE carTypeCasted = (CAR_TYPE) carTypeCreated;

            CarTypeResponse carTypeDto = new CarTypeResponse(carTypeCasted.getId(),carTypeCasted.getName());
            return Response.status(Response.Status.CREATED).entity(carTypeDto).build();

        }catch(JsonProcessingException e){
            System.err.println(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

    }

    @DELETE
    @Path("/cartypes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCarType(@HeaderParam(HttpHeaders.AUTHORIZATION) String Athetization, @PathParam("id") Long id){
        USER userAuth = getUserAuth(Athetization);
        if (userAuth == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Object carTypeDeleted = carParkService.deleteCarType(id);
        if (carTypeDeleted == null)
            return Response.status(Response.Status.BAD_REQUEST).build();

        return Response.status(Response.Status.NO_CONTENT).build();

    }
}
