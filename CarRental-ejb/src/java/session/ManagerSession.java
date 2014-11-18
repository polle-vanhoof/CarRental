package session;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Set<CarType> getCarTypes(String company) {
        try {
            Query q = em.createQuery(
                "SELECT ctype FROM CarRentalCompany crc JOIN crc.carTypes ctype "
                +"WHERE crc.name= :company")
                .setParameter("company", company);
            return new HashSet<CarType>(q.getResultList());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        try {
            Query q = em.createQuery(
                "SELECT c.id FROM CarRentalCompany crc JOIN crc.cars c "
                +"WHERE c.type = :type AND crc.name = :company")
                .setParameter("type", type)
                .setParameter("company", company);
            return new HashSet<Integer>(q.getResultList());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        try {
            Query q = em.createQuery(
            "SELECT COUNT(res.id) FROM CarRentalCompany crc JOIN crc.cars c JOIN c.reservations res "
            + "WHERE crc.name = :company AND c.type = :type AND c.id = :id")
            .setParameter("company", company)
            .setParameter("type", type)
            .setParameter("id", id);
            return q.getFirstResult();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        try {
            Query q = em.createQuery(
            "SELECT COUNT(res.id) FROM CarRentalCompany crc JOIN crc.cars c JOIN c.reservations res "
            + "WHERE crc.name = :company AND c.type = :type")
            .setParameter("company", company)
            .setParameter("type", type);
            return q.getFirstResult();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public int getNumberOfReservationsBy(String renter) {
        Query q = em.createQuery(
            "SELECT COUNT(res.id) FROM Reservation res "
            + "WHERE res.carRenter = :renter")
            .setParameter("renter", renter);
            return q.getFirstResult();
    }

    @Override
    public int addCarType(CarType carType){
        em.persist(carType);
        return carType.getID();
    }
    
    @Override 
    public void addCar(int carID, int typeID){
        CarType type = em.find(CarType.class, typeID);
        Car car = new Car(carID, type);
        em.persist(car);
    }
    
    @Override
    public void addCompany(String companyName, List<Integer> carIDs) {
        LinkedList<Car> cars = new LinkedList<Car>();
        
        for(int carID : carIDs){
            cars.add(em.find(Car.class, carID));
        }
        
        CarRentalCompany company = new CarRentalCompany(companyName, cars);
        em.persist(company);
    }
    
    
}