package session;

import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import javax.persistence.EntityExistsException;
import rental.CarType;

@Remote
public interface ManagerSessionRemote {
    
    public Set<CarType> getCarTypes(String company);
    
    public Set<Integer> getCarIds(String company,String type);
    
    public int getNumberOfReservations(String company, String type, int carId);
    
    public int getNumberOfReservations(String company, String type);
      
    public int getNumberOfReservationsBy(String renter);
    
    public void addCompany(String companyName, List<Integer> carIDs);
    
    public int addCarType(CarType carType);
    
    public void addCar(int carID, int typeID);
    
    public CarType getMostPopularCarTypeIn(String crc);
    
    public Set<String> getBestClients();
}