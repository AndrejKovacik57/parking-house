package sk.stuba.fei.uim.vsa.pr2.service;


import sk.stuba.fei.uim.vsa.pr2.domain.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.text.SimpleDateFormat;
import java.util.*;

public class CarParkService extends AbstractCarParkService{
    public Object persist(EntityManager em, Object object){
        try {
            em.getTransaction().begin();
            em.persist(object);
            em.getTransaction().commit();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
        return object;
    }

    public Object merge(EntityManager em, Object mergeObject){
        Object managedMergeObject;
        try{
            em.getTransaction().begin();
            managedMergeObject = em.merge(mergeObject);
            em.getTransaction().commit();
        }catch (Exception e){
            return null;
        } finally {
            em.close();
        }
        return managedMergeObject;
    }

    public Object mergeDouble(EntityManager em, Object mergeObject, Object mergeObject2){
        Object managedMergeObject;
        try{
            em.getTransaction().begin();
            em.merge(mergeObject);
            managedMergeObject = em.merge(mergeObject2);
            em.getTransaction().commit();
        }catch (Exception e){
            return null;
        } finally {
            em.close();
        }
        return managedMergeObject;
    }
    public Object remove(EntityManager em, Object object){
        try {
            em.getTransaction().begin();
            em.remove(object);
            em.getTransaction().commit();
        } catch (Exception e) {
//            em.getTransaction().rollback();
            return null;
        } finally {
            em.close();
        }
        return object;
    }

    /*
      OSETRI VSTUPY PRE CREATE FUNCKIE
      OSETRI VSTUPY PRE CREATE FUNCKIE
      OSETRI VSTUPY PRE CREATE FUNCKIE
      OSETRI VSTUPY PRE CREATE FUNCKIE
      */
    @Override
    public Object createCarPark(String name, String address, Integer pricePerHour) {
        if (name == null || address == null || pricePerHour == null){
            return null;
        }
        EntityManager em = emf.createEntityManager();

        TypedQuery<CAR_PARK> q = em.createQuery("SELECT cp FROM CAR_PARK cp WHERE cp.name=:name", CAR_PARK.class)
                .setParameter("name", name);

        if(!q.getResultList().isEmpty()){
            em.close();
            return null;
        }

        CAR_PARK carPark = new CAR_PARK(address, name, pricePerHour);

        return persist(em, carPark);
    }

    @Override
    public Object getCarPark(Long carParkId) {
        EntityManager em = emf.createEntityManager();
        CAR_PARK carPark;
        carPark = em.find(CAR_PARK.class, carParkId);

        if(carPark == null){
            em.close();
            return null;
        }
        em.close();

        return carPark;
    }

    @Override
    public Object getCarPark(String carParkName) {
        EntityManager em = emf.createEntityManager();

        TypedQuery<CAR_PARK> q = em.createQuery("SELECT cp FROM CAR_PARK cp WHERE cp.name=:carParkName", CAR_PARK.class)
                .setParameter("carParkName", carParkName);

        if(q.getResultList().isEmpty() ){
            em.close();
            return null;
        }
        CAR_PARK carPark = q.getSingleResult();
        em.close();

        return carPark;
    }

    @Override
    public List<Object> getCarParks() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<CAR_PARK> q = em.createQuery("SELECT cp FROM CAR_PARK cp", CAR_PARK.class);

        List<Object> cpList = new ArrayList<Object>(q.getResultList());
        em.close();

        return cpList;
    }

    @Override
    public Object updateCarPark(Object carPark) {
        if(!(carPark instanceof CAR_PARK)){
            return null;
        }
        EntityManager em = emf.createEntityManager();
        CAR_PARK carParkCasted = (CAR_PARK) carPark;

        CAR_PARK carParkExists = em.find(CAR_PARK.class, carParkCasted.getId());

        if(carParkExists == null){
            em.close();
            return null;
        }
        if (carParkCasted.getName() == null || carParkCasted.getAddress() == null || carParkCasted.getPricePerHour() == null){
            return null;
        }

        if(!carParkCasted.getName().equals(carParkExists.getName())){
            TypedQuery<CAR_PARK> q = em.createQuery("SELECT cp FROM CAR_PARK cp WHERE cp.name=:name", CAR_PARK.class)
                    .setParameter("name", carParkCasted.getName());

            if(!q.getResultList().isEmpty()){
                em.close();
                return null;
            }
        }

        carParkExists.setAddress(carParkCasted.getAddress());
        carParkExists.setName(carParkCasted.getName());
        carParkExists.setPricePerHour(carParkCasted.getPricePerHour());

        return merge(em, carParkExists);
    }

    @Override
    public Object deleteCarPark(Long carParkId) {

        EntityManager em = emf.createEntityManager();

        CAR_PARK carPark = em.find(CAR_PARK.class, carParkId);
        if (carPark == null){

            em.close();
            return null;
        }

        for(CAR_PARK_FLOOR carParkFloor: carPark.getFloors()){
            deleteCarParkFloor(carParkFloor.getId().getCarParkId(), carParkFloor.getId().getFloorIdentifier());
        }

        return remove(em, carPark);
    }

    @Override
    public Object createCarParkFloor(Long carParkId, String floorIdentifier) {
        if (carParkId == null || floorIdentifier == null){
            return null;
        }
        EntityManager em = emf.createEntityManager();

        CAR_PARK carPark = em.find(CAR_PARK.class, carParkId);
        if (carPark == null){
            em.close();
            return null;
        }

        for(CAR_PARK_FLOOR floor: carPark.getFloors()){
            if(floor.getId().getFloorIdentifier().equals(floorIdentifier)){
                em.close();
                return null;
            }
        }
        CAR_PARK_FLOOR carParkFloor = new CAR_PARK_FLOOR(new CAR_PARK_FLOOR_ID( carParkId, floorIdentifier), carPark);

        carPark.getFloors().add(carParkFloor);

        CAR_PARK carParkMerged = ((CAR_PARK)merge(em, carPark));
        return carParkMerged.getFloors().get(carParkMerged.getFloors().size()-1);
    }

    @Override
    public Object getCarParkFloor(Long carParkId, String floorIdentifier) {
        EntityManager em = emf.createEntityManager();

        CAR_PARK_FLOOR carParkFloor = em.find(CAR_PARK_FLOOR.class, new CAR_PARK_FLOOR_ID(carParkId, floorIdentifier));
        if (carParkFloor == null){
            em.close();
            return null;
        }
        em.close();
        return carParkFloor;
    }

    @Override //mam kompozit. nerobit
    public Object getCarParkFloor(Long carParkFloorId) {
        return null;
    }

    @Override
    public List<Object> getCarParkFloors(Long carParkId) {
        EntityManager em = emf.createEntityManager();
        CAR_PARK carPark= em.find(CAR_PARK.class,carParkId);

        if (carPark == null){
            em.close();
            return null;
        }

        TypedQuery<CAR_PARK_FLOOR> q = em.createQuery("SELECT cpf FROM CAR_PARK_FLOOR cpf where cpf.id.carParkId=:carParkId", CAR_PARK_FLOOR.class)
                .setParameter("carParkId", carParkId);

        List<Object> cpfList = new ArrayList<Object>(q.getResultList());
        em.close();

        return cpfList;
    }

    @Override  //mam kompozit. nerobit
    public Object updateCarParkFloor(Object carParkFloor) {
        return null;
    }

    @Override
    public Object deleteCarParkFloor(Long carParkId, String floorIdentifier) {
        EntityManager em = emf.createEntityManager();

        CAR_PARK_FLOOR carParkFloor = em.find(CAR_PARK_FLOOR.class, new CAR_PARK_FLOOR_ID(carParkId, floorIdentifier));
        if (carParkFloor == null){
            em.close();
            return null;
        }

        for(PARKING_SPOT parkingSpot: carParkFloor.getParkingSpots()){
            deleteParkingSpot(parkingSpot.getId());
        }
        CAR_PARK carPark = em.find(CAR_PARK.class, carParkFloor.getCarPark().getId());
        carPark.getFloors().removeIf(carParkFloorToDelete -> carParkFloorToDelete.getId().equals(carParkFloor.getId()));

        return merge(em, carPark);

    }

    @Override //mam kompozit. nerobit
    public Object deleteCarParkFloor(Long carParkFloorId) {
        return null;
    }


    private CAR_TYPE findOrCreateCarType (EntityManager em){
        CAR_TYPE carType;
        TypedQuery<CAR_TYPE> q2 = em.createQuery("SELECT ct from CAR_TYPE ct WHERE ct.name=:name", CAR_TYPE.class)
                .setParameter("name", "benzin");
        if(q2.getResultList().isEmpty()){
            carType = em.find(CAR_TYPE.class, ((CAR_TYPE) createCarType("benzin")).getId());

        }else{
            carType = q2.getSingleResult();
        }
        return carType;
    }

    @Override
    public Object createParkingSpot(Long carParkId, String floorIdentifier, String spotIdentifier) {
       if(carParkId==null || floorIdentifier==null || spotIdentifier==null){
           return null;
       }
        EntityManager em = emf.createEntityManager();
        CAR_PARK carPark = em.find(CAR_PARK.class, carParkId);
        if (carPark == null){
            em.close();
            return null;
        }
        CAR_TYPE carType;

        for(CAR_PARK_FLOOR floor: carPark.getFloors()){
            if(floor.getId().getFloorIdentifier().equals(floorIdentifier)){
                TypedQuery<PARKING_SPOT> q = em.createQuery("SELECT ps from PARKING_SPOT ps WHERE ps.spotIdentifier=:spotIdentifier AND ps.carParkFloor.carPark.id=:carParkId", PARKING_SPOT.class)
                        .setParameter("spotIdentifier", spotIdentifier)
                        .setParameter("carParkId", carParkId);

                if(q.getResultList().isEmpty()){
                    PARKING_SPOT parkingSpot = new PARKING_SPOT(spotIdentifier);
                    parkingSpot.setCarParkFloor(floor);

                    carType = findOrCreateCarType(em);

                    parkingSpot.setCarType(carType);
                    carType.getParkingSpots().add(parkingSpot);
                    floor.getParkingSpots().add(parkingSpot);


                    CAR_TYPE carTypeManaged = (CAR_TYPE)mergeDouble(em, floor, carType);

                    return carTypeManaged.getParkingSpots().get(carTypeManaged.getParkingSpots().size()-1);


                }else{
                    em.close();
                    return null;
                }

            }
        }

        em.close();
        return null;
    }

    @Override
    public Object getParkingSpot(Long parkingSpotId) {
        EntityManager em = emf.createEntityManager();

        PARKING_SPOT parkingSpot  = em.find(PARKING_SPOT.class, parkingSpotId);
        if (parkingSpot == null){
            em.close();
            return null;
        }
        em.close();
        return parkingSpot;
    }

    @Override
    public List<Object> getParkingSpots(Long carParkId, String floorIdentifier) {
        EntityManager em = emf.createEntityManager();
        CAR_PARK_FLOOR carParkFloor = em.find(CAR_PARK_FLOOR.class, new CAR_PARK_FLOOR_ID(carParkId, floorIdentifier));
        if(carParkFloor == null){
            em.close();
            return null;
        }

        TypedQuery<PARKING_SPOT> q = em.createQuery("SELECT ps from PARKING_SPOT ps WHERE ps.carParkFloor.id.floorIdentifier=:floorIdentifier AND ps.carParkFloor.carPark.id=:carParkId", PARKING_SPOT.class)
                .setParameter("floorIdentifier", floorIdentifier)
                .setParameter("carParkId", carParkId);


        List<Object> psList = new ArrayList<Object>(q.getResultList());
        em.close();

        return psList;
    }


    @Override
    public Map<String, List<Object>> getParkingSpots(Long carParkId) {
        EntityManager em = emf.createEntityManager();
        CAR_PARK carPark = em.find(CAR_PARK.class, carParkId);

        if (carPark == null){
            em.close();
            return null;
        }
        Map<String, List<Object>> parkingSpotsMap = new HashMap<String, List<Object>>();

        carPark.getFloors().forEach(floor -> {
            parkingSpotsMap.put(floor.getId().getFloorIdentifier(),  new ArrayList<Object>(floor.getParkingSpots()));
        });

        em.close();

        return parkingSpotsMap;
    }

    private Map<String, List<Object>> mapFillParkingSpots(EntityManager em, List<PARKING_SPOT> parkingSpots) {
        Map<String, List<Object>>parkingSpotsMap = new HashMap<String, List<Object>>();

        parkingSpots.forEach(parkingSpot -> {
            String floorIdentifier = parkingSpot.getCarParkFloor().getId().getFloorIdentifier();
            if(!parkingSpotsMap.containsKey(floorIdentifier)){
                parkingSpotsMap.put(floorIdentifier, new ArrayList<Object>());
            }
            parkingSpotsMap.get(floorIdentifier).add(parkingSpot);
        });
        em.close();
        return parkingSpotsMap;
    }

    @Override
    public Map<String, List<Object>> getAvailableParkingSpots(String carParkName) {
        EntityManager em = emf.createEntityManager();
        if(getCarPark(carParkName) == null){
            em.close();
            return null;
        }
        TypedQuery<PARKING_SPOT> q = em.createQuery("SELECT ps FROM PARKING_SPOT ps WHERE ps.occupied=false AND ps.carParkFloor.carPark.name=:carParkName", PARKING_SPOT.class)
                .setParameter("carParkName", carParkName);

        return mapFillParkingSpots(em, q.getResultList());
    }

    @Override
    public Map<String, List<Object>> getOccupiedParkingSpots(String carParkName) {
        EntityManager em = emf.createEntityManager();
        if(getCarPark(carParkName) == null){
            em.close();
            return null;
        }

        TypedQuery<PARKING_SPOT> q = em.createQuery("SELECT ps FROM PARKING_SPOT ps WHERE ps.occupied=true AND ps.carParkFloor.carPark.name=:carParkName", PARKING_SPOT.class)
                .setParameter("carParkName", carParkName);

        return mapFillParkingSpots(em, q.getResultList());
    }

    @Override
    public Object updateParkingSpot(Object parkingSpot) {
        if(!(parkingSpot instanceof PARKING_SPOT)){
            return null;
        }
        EntityManager em = emf.createEntityManager();
        PARKING_SPOT parkingSpotCasted = (PARKING_SPOT) parkingSpot;

        PARKING_SPOT parkingSpotExists = em.find(PARKING_SPOT.class, parkingSpotCasted.getId());

        if(parkingSpotExists == null){
            em.close();
            return null;
        }
        if (parkingSpotCasted.getSpotIdentifier() == null ){
            em.close();
            return null;
        }
        if(!parkingSpotCasted.getSpotIdentifier().equals(parkingSpotExists.getSpotIdentifier())){
            TypedQuery<PARKING_SPOT> q = em.createQuery("SELECT ps from PARKING_SPOT ps WHERE ps.spotIdentifier=:spotIdentifier AND ps.carParkFloor.carPark.id=:carParkId", PARKING_SPOT.class)
                    .setParameter("spotIdentifier", parkingSpotCasted.getSpotIdentifier())
                    .setParameter("carParkId", parkingSpotCasted.getCarParkFloor().getCarPark().getId());
            if(!q.getResultList().isEmpty()){
                em.close();
                return null;
            }
        }
        parkingSpotExists.setSpotIdentifier(parkingSpotCasted.getSpotIdentifier());

        return merge(em, parkingSpotExists);
    }

    @Override
    public Object deleteParkingSpot(Long parkingSpotId) {
        EntityManager em = emf.createEntityManager();
        PARKING_SPOT parkingSpot = em.find(PARKING_SPOT.class, parkingSpotId);
        if(parkingSpot == null){
            em.close();
            return null;
        }
        TypedQuery<RESERVATION> q = em.createQuery("SELECT r FROM RESERVATION r WHERE r.parkingSpot.id=:id", RESERVATION.class)
                .setParameter("id", parkingSpotId);

        if(!q.getResultList().isEmpty()){
            for(RESERVATION reservation: q.getResultList()) {
                if (reservation.getEndDate() == null){
                    endReservation(reservation.getId());
                }

                reservation.setParkingSpot(null);
                try {
                    em.getTransaction().begin();
                    em.merge(reservation);
                    em.getTransaction().commit();
                }catch (Exception e){
                    return null;
                }
            }


        }

        CAR_TYPE carType = em.find(CAR_TYPE.class, parkingSpot.getCarType().getId());
        carType.getParkingSpots().removeIf(parkingSpotToDelete -> parkingSpotToDelete.getId().equals(parkingSpotId));

        CAR_PARK_FLOOR carParkFloor = em.find(CAR_PARK_FLOOR.class, parkingSpot.getCarParkFloor().getId());
        carParkFloor.getParkingSpots().removeIf(parkingSpotToDelete -> parkingSpotToDelete.getId().equals(parkingSpotId));

        mergeDouble(em, carType, carParkFloor);
        return parkingSpot;
    }

    @Override
    public Object createCar(Long userId, String brand, String model, String colour, String vehicleRegistrationPlate) {
        if(userId==null || brand==null || model==null|| colour==null|| vehicleRegistrationPlate==null){
            return null;
        }

        EntityManager em = emf.createEntityManager();

        USER user = em.find(USER.class, userId);
        if (user == null){
            em.close();
            return null;
        }
        TypedQuery<CAR> q = em.createQuery("SELECT c FROM CAR c WHERE c.vehicleRegistrationPlate=:plate", CAR.class)
                .setParameter("plate", vehicleRegistrationPlate);

        if(!q.getResultList().isEmpty()){
            em.close();
            return null;
        }
        CAR car = new CAR(vehicleRegistrationPlate, brand, model, colour);
        car.setUser(user);

        CAR_TYPE carType = findOrCreateCarType (em);
        car.setCarType(carType);

        carType.getCars().add(car);
        user.getCars().add(car);

        CAR_TYPE carTypeManaged = (CAR_TYPE)mergeDouble(em, user, carType);

        return carTypeManaged.getCars().get(carTypeManaged.getCars().size()-1);
    }

    @Override
    public Object getCar(Long carId) {
        EntityManager em = emf.createEntityManager();

        CAR car = em.find(CAR.class, carId);
        if (car == null){
            em.close();
            return null;
        }
        em.close();
        return car;

    }

    @Override
    public Object getCar(String vehicleRegistrationPlate) {
        EntityManager em = emf.createEntityManager();

        TypedQuery<CAR> q = em.createQuery("SELECT c FROM CAR c WHERE c.vehicleRegistrationPlate=:plate", CAR.class)
                .setParameter("plate", vehicleRegistrationPlate);

        if(q.getResultList().isEmpty() ){
            em.close();
            return null;
        }
        CAR car = q.getSingleResult();
        em.close();

        return car;
    }

    @Override
    public List<Object> getCars(Long userId) {
        EntityManager em = emf.createEntityManager();
        USER user = em.find(USER.class, userId);

        if(user == null){
            em.close();
            return null;
        }

        TypedQuery<CAR> q = em.createQuery("SELECT c FROM CAR c where c.user.id=:userId", CAR.class)
                .setParameter("userId", userId);

        List<Object> cList = new ArrayList<Object>(q.getResultList());
        em.close();

        return cList;
    }
    @Override
    public List<Object> getCars() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<CAR> q = em.createQuery("SELECT c FROM CAR c", CAR.class);

        List<Object> cpList = new ArrayList<Object>(q.getResultList());
        em.close();

        return cpList;
    }

    @Override
    public Object updateCar(Object car) {
        if(!(car instanceof CAR)){
            return null;
        }
        EntityManager em = emf.createEntityManager();
        CAR carCasted = (CAR) car;

        CAR carExists = em.find(CAR.class, carCasted.getId());

        if(carExists == null){
            em.close();
            return null;
        }
        if (carCasted.getVehicleRegistrationPlate() == null || carCasted.getBrand() == null || carCasted.getColour() == null ||
            carCasted.getModel() == null){
            return null;
        }

        if(!carCasted.getVehicleRegistrationPlate() .equals(carExists.getVehicleRegistrationPlate())){
            TypedQuery<CAR> q = em.createQuery("SELECT c FROM CAR c WHERE c.vehicleRegistrationPlate=:vehicleRegistrationPlate", CAR.class)
                    .setParameter("vehicleRegistrationPlate", carCasted.getVehicleRegistrationPlate());

            if(!q.getResultList().isEmpty()){
                em.close();
                return null;
            }
        }

        carExists.setVehicleRegistrationPlate(carCasted.getVehicleRegistrationPlate());
        carExists.setBrand(carCasted.getBrand());
        carExists.setColour(carExists.getColour());
        carExists.setModel(carCasted.getModel());

        return merge(em, carExists);
    }

    @Override
    public Object deleteCar(Long carId) {
        EntityManager em = emf.createEntityManager();

        CAR car = em.find(CAR.class, carId);
        if(car == null){
            em.close();
            return null;
        }

        TypedQuery<RESERVATION> q = em.createQuery("SELECT r FROM RESERVATION r WHERE r.car.id=:id", RESERVATION.class)
                .setParameter("id", carId);
        if(!q.getResultList().isEmpty()){
            RESERVATION reservation = q.getSingleResult();

            if (reservation.getEndDate() == null){
                endReservation(reservation.getId());
            }

            reservation.setCar(null);
            try {
                em.getTransaction().begin();
                em.merge(reservation);
                em.getTransaction().commit();
            }catch (Exception e){
                return null;
            }
        }

        CAR_TYPE carType = em.find(CAR_TYPE.class, car.getCarType().getId());
        carType.getCars().removeIf(carToDelete -> carToDelete.getId().equals(carId));


        USER user = em.find(USER.class, car.getUser().getId());
        user.getCars().removeIf(carToDelete -> carToDelete.getId().equals(carId));


        mergeDouble(em, carType, user);
        return car;
    }

    @Override
    public Object createUser(String firstname, String lastname, String email) {
        if(firstname==null || lastname==null || email==null){
            return null;
        }
        EntityManager em = emf.createEntityManager();
        TypedQuery<USER> q = em.createQuery("SELECT u FROM USER u WHERE u.email=:email", USER.class)
                .setParameter("email", email);

        if(!q.getResultList().isEmpty()){
            em.close();
            return null;
        }
        USER user = new USER(firstname, lastname, email);

        return persist(em, user);
    }

    @Override
    public Object getUser(Long userId) {
        EntityManager em = emf.createEntityManager();
        USER user = em.find(USER.class, userId);

        if(user == null){
            em.close();
            return null;
        }
        em.close();

        return user;
    }

    @Override
    public Object getUser(String email) {
        EntityManager em = emf.createEntityManager();
        USER user;
        TypedQuery<USER> q = em.createQuery("SELECT u FROM USER u WHERE u.email=:email", USER.class)
                .setParameter("email", email);

        if(q.getResultList().isEmpty() ){
            em.close();
            return null;
        }
        user = q.getSingleResult();
        em.close();

        return user;
    }

    @Override
    public List<Object> getUsers() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<USER> q = em.createQuery("SELECT u FROM USER u", USER.class);

        List<Object> cpList = new ArrayList<Object>(q.getResultList());
        em.close();

        return cpList;
    }

    @Override
    public Object updateUser(Object user) {
        if(!(user instanceof USER)){
            return null;
        }
        EntityManager em = emf.createEntityManager();
        USER userCasted = (USER) user;

        USER userExists = em.find(USER.class, userCasted.getId());

        if(userExists == null){
            em.close();
            return null;
        }
        if (userCasted.getFirstName() == null || userCasted.getLastName() == null || userCasted.getEmail() == null){
            return null;
        }

        if(!userCasted.getEmail().equals(userExists.getEmail())){
            TypedQuery<USER> q = em.createQuery("SELECT u FROM USER u WHERE u.email=:email", USER.class)
                    .setParameter("email", userCasted.getEmail());

            if(!q.getResultList().isEmpty()){
                em.close();
                return null;
            }
        }

        userExists.setFirstName(userCasted.getFirstName());
        userExists.setLastName(userCasted.getLastName());
        userExists.setEmail(userCasted.getEmail());

        return merge(em, userExists);
    }

    @Override
    public Object deleteUser(Long userId) {
        EntityManager em = emf.createEntityManager();
        USER user = em.find(USER.class, userId);

        if(user == null){
            em.close();
            return null;
        }
        for(CAR car: user.getCars()){
            deleteCar(car.getId());
        }

        return remove(em, user);
    }

    @Override
    public Object createReservation(Long parkingSpotId, Long cardId) {
        if(cardId==null || parkingSpotId==null){
            return null;
        }
        EntityManager em = emf.createEntityManager();

        CAR car = em.find(CAR.class, cardId);

        if(car == null){
            em.close();
            return null;
        }
        for(RESERVATION r: car.getReservations()){
            if (r.getEndDate() == null){
                em.close();
                return null;
            }
        }
        PARKING_SPOT parkingSpot = em.find(PARKING_SPOT.class, parkingSpotId);

        if(parkingSpot == null){
            em.close();
            return null;
        }

        if(!parkingSpot.getCarType().equals(car.getCarType())){
            em.close();
            return null;
        }

        for(RESERVATION r: parkingSpot.getReservations()){
            if (r.getEndDate() == null){
                em.close();
                return null;
            }
        }

        parkingSpot.setOccupied(true);
        RESERVATION reservation = new RESERVATION(new Date(), car, parkingSpot);
        car.getReservations().add(reservation);
        parkingSpot.getReservations().add(reservation);
        try{
            em.getTransaction().begin();
            em.persist(reservation);
            em.merge(car);
            em.merge(parkingSpot);
            em.getTransaction().commit();
        }catch (Exception e){
            e.printStackTrace();
            em.getTransaction().rollback();
            return null;
        } finally {
            em.close();
        }
        return reservation;
    }

    @Override
    public Object endReservation(Long reservationId) {
        EntityManager em = emf.createEntityManager();

        if(reservationId == null){
            em.close();
            return null;
        }

        RESERVATION reservation = em.find(RESERVATION.class, reservationId);

        if(reservation == null){
            em.close();
            return null;
        }
        if(reservation.getEndDate() != null){
            em.close();
            return null;
        }
        //skontrolovat spravanie ak je hodina a sekunda do dalsej hodiny
        reservation.setEndDate(new Date());
        Integer pricePerHour = reservation.getParkingSpot().getCarParkFloor().getCarPark().getPricePerHour();
        Long secondsPassed = (reservation.getDate().getTime() - reservation.getEndDate().getTime())/1000; //compiler sa o to postara lebo ak pouzijem Long.valueOf() tak mi nadava ze zbytocne obalujem
        Long hoursPassed;

        if(secondsPassed % 60 == 0) {
            Long minutesPassed = secondsPassed / 60;

            if(minutesPassed % 60 == 0)
                hoursPassed = minutesPassed / 60;
            else
                hoursPassed = (minutesPassed / 60) + 1;

        } else
            hoursPassed = (secondsPassed / 60 / 60) + 1;

        hoursPassed = (hoursPassed == 0 ? 1 : hoursPassed);

        reservation.setParkingCost(Math.toIntExact(hoursPassed * pricePerHour));
        reservation.getParkingSpot().setOccupied(false);


        return merge(em, reservation);
    }

    @Override
    public List<Object> getReservations(Long parkingSpotId, Date date) {
        EntityManager em = emf.createEntityManager();

        PARKING_SPOT parkingSpot  = em.find(PARKING_SPOT.class, parkingSpotId);

        if(parkingSpot == null){
            em.close();
            return null;
        }
        List<Object> reservationList = new ArrayList<Object>();
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");

        parkingSpot.getReservations().forEach( reservation -> {
                if (fmt.format(date).equals(fmt.format(reservation.getDate()))){
                    reservationList.add(reservation);
                }
        });

        return reservationList;
    }
    @Override
    public List<Object> getReservations() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<RESERVATION> q = em.createQuery("SELECT r FROM RESERVATION r", RESERVATION.class);

        List<Object> reservationList = new ArrayList<Object>(q.getResultList());
        em.close();

        return reservationList;
    }

    @Override
    public List<Object> getMyReservations(Long userId) {
        EntityManager em = emf.createEntityManager();

        USER user  = em.find(USER.class, userId);
        if (user == null){
            em.close();
            return null;
        }
        List<Object> reservationList = new ArrayList<Object>();
        user.getCars().forEach(car -> {
            car.getReservations().forEach(reservation -> {
                if(reservation.getEndDate() == null){
                    reservationList.add(reservation);
                }
            });
        });

        return reservationList;
    }
    @Override
    public Object getReservation(Long reservationId) {
        EntityManager em = emf.createEntityManager();
        RESERVATION reservation;
        reservation = em.find(RESERVATION.class, reservationId);

        if(reservation == null){
            em.close();
            return null;
        }
        em.close();

        return reservation;
    }

    @Override
    public Object updateReservation(Object reservation) {
        if(!(reservation instanceof RESERVATION)){
            return null;
        }
        EntityManager em = emf.createEntityManager();
        RESERVATION reservationCasted = (RESERVATION) reservation;

        RESERVATION reservationExists = em.find(RESERVATION.class, reservationCasted.getId());

        if(reservationExists == null){
            em.close();
            return null;
        }
        if (reservationExists.getEndDate() != null){
            return null;
        }

        reservationExists.setDate(reservationCasted.getDate());

        return merge(em, reservationExists);
    }

    @Override
    public Object createCarType(String name) {
        if(name==null){
            return null;
        }
        EntityManager em = emf.createEntityManager();

        TypedQuery<CAR_TYPE> q = em.createQuery("SELECT ct from CAR_TYPE ct WHERE ct.name=:name", CAR_TYPE.class)
                .setParameter("name", name);

        if(!q.getResultList().isEmpty()){
            em.close();
            return null;
        }

        CAR_TYPE carType = new CAR_TYPE(name);

        return persist(em, carType);
    }

    @Override
    public List<Object> getCarTypes() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<CAR_TYPE> q = em.createQuery("SELECT ct FROM CAR_TYPE ct", CAR_TYPE.class);

        List<Object> ctList = new ArrayList<Object>(q.getResultList());
        em.close();

        return ctList;
    }

    @Override
    public Object getCarType(Long carTypeId) {
        EntityManager em = emf.createEntityManager();
        CAR_TYPE carType;
        carType = em.find(CAR_TYPE.class, carTypeId);

        if(carType == null){
            em.close();
            return null;
        }
        em.close();

        return carType;
    }

    @Override
    public Object getCarType(String name) {
        EntityManager em = emf.createEntityManager();

        TypedQuery<CAR_TYPE> q = em.createQuery("SELECT ct FROM CAR_TYPE ct WHERE ct.name=:name", CAR_TYPE.class)
                .setParameter("name", name);

        if(q.getResultList().isEmpty() ){
            em.close();
            return null;
        }
        CAR_TYPE carType = q.getSingleResult();
        em.close();

        return carType;
    }

    @Override
    public Object deleteCarType(Long carTypeId) {
        EntityManager em = emf.createEntityManager();

        if (carTypeId == null){
            em.close();
            return null;
        }

        CAR_TYPE carType = em.find(CAR_TYPE.class, carTypeId);
        if(carType == null){
            em.close();
            return null;
        }

        if (!carType.getCars().isEmpty()){
            em.close();
            return null;
        }
        if (!carType.getParkingSpots().isEmpty()){
            em.close();
            return null;
        }
        return remove(em, carType);
    }

    @Override
    public Object createCar(Long userId, String brand, String model, String colour, String vehicleRegistrationPlate, Long carTypeId) {
        if(userId==null || brand==null || model==null|| colour==null|| vehicleRegistrationPlate==null || carTypeId==null){
            return null;
        }
        EntityManager em = emf.createEntityManager();

        USER user = em.find(USER.class, userId);
        if (user == null){
            em.close();
            return null;
        }
        TypedQuery<CAR> q = em.createQuery("SELECT c FROM CAR c WHERE c.vehicleRegistrationPlate=:plate", CAR.class)
                .setParameter("plate", vehicleRegistrationPlate);

        if(!q.getResultList().isEmpty()){
            em.close();
            return null;
        }

        CAR_TYPE carType = em.find(CAR_TYPE.class, carTypeId);
        if (carType == null){
            em.close();
            return null;
        }

        CAR car = new CAR(vehicleRegistrationPlate, brand, model, colour);
        car.setUser(user);
        car.setCarType(carType);

        carType.getCars().add(car);
        user.getCars().add(car);

        CAR_TYPE carTypeManaged = (CAR_TYPE)mergeDouble(em, user, carType);

        return carTypeManaged.getCars().get(carTypeManaged.getCars().size()-1);
    }

    @Override
    public Object createParkingSpot(Long carParkId, String floorIdentifier, String spotIdentifier, Long carTypeId) {
        if(carParkId==null|| floorIdentifier==null|| spotIdentifier==null || carTypeId==null){
            return null;
        }
        EntityManager em = emf.createEntityManager();

        CAR_PARK carPark = em.find(CAR_PARK.class, carParkId);
        if (carPark == null){
            em.close();
            return null;
        }

        CAR_TYPE carType = em.find(CAR_TYPE.class, carTypeId);
        if (carType == null){
            em.close();
            return null;
        }

        for(CAR_PARK_FLOOR floor: carPark.getFloors()){
            if(floor.getId().getFloorIdentifier().equals(floorIdentifier)){
                TypedQuery<PARKING_SPOT> q = em.createQuery("SELECT ps from PARKING_SPOT ps WHERE ps.spotIdentifier=:spotIdentifier AND ps.carParkFloor.carPark.id=:carParkId", PARKING_SPOT.class)
                        .setParameter("spotIdentifier", spotIdentifier)
                        .setParameter("carParkId", carParkId);

                if(q.getResultList().isEmpty()){
                    PARKING_SPOT parkingSpot = new PARKING_SPOT(spotIdentifier);
                    parkingSpot.setCarParkFloor(floor);
                    parkingSpot.setCarType(carType);

                    carType.getParkingSpots().add(parkingSpot);
                    floor.getParkingSpots().add(parkingSpot);

                    CAR_TYPE carTypeManaged = (CAR_TYPE)mergeDouble(em, floor, carType);

                    return carTypeManaged.getParkingSpots().get(carTypeManaged.getParkingSpots().size()-1);


                }else{
                    em.close();
                    return null;
                }

            }
        }

        em.close();
        return null;
    }
}
