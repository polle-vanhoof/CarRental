package session;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class CarRentalSession implements CarRentalSessionRemote {

    @PersistenceContext
    private EntityManager em;

    @Resource
    private SessionContext context;

    private String renter;
    private List<Quote> quotes = new LinkedList<Quote>();

    @Override
    public Set<String> getAllRentalCompanies() {
        try {
            Query q = em.createQuery(
                    "SELECT crc.name FROM CarRentalCompany crc");
            return new HashSet<String>(q.getResultList());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CarRentalSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        try {
            Query q = em.createQuery(
                    "SELECT DISTINCT c.type FROM CarRentalCompany crc JOIN crc.cars c WHERE c.id NOT IN "
                    + "(SELECT res.carId FROM Reservation res "
                    + "WHERE res.startDate<=:end OR res.endDate>=:start)")
                    .setParameter("start", start)
                    .setParameter("end", end);
            return new LinkedList<CarType>(q.getResultList());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CarRentalSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public String getCheapestCarType(Date start, Date end) {
        Query q = em.createQuery(
                "SELECT DISTINCT c.type FROM CarRentalCompany crc JOIN crc.cars c JOIN c.type ct WHERE c.id NOT IN "
                + "(SELECT res.carId FROM Reservation res "
                + "WHERE res.startDate<=:end OR res.endDate>=:start)"
                + "ORDER BY ct.rentalPricePerDay ASC")
                .setParameter("start", start)
                .setParameter("end", end);
        LinkedList<CarType> orderedTypes = new LinkedList<CarType>(q.getResultList());
        return orderedTypes.getFirst().getName();
    }

    @Override
    public Quote createQuote(String company, ReservationConstraints constraints) throws ReservationException {
        try {
            CarRentalCompany crc = em.find(CarRentalCompany.class, company);
            Quote out = crc.createQuote(constraints, renter);
            quotes.add(out);
            return out;
        } catch (Exception e) {
            throw new ReservationException(e);
        }
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @Override
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<Reservation>();
        try {
            for (Quote quote : quotes) {
                CarRentalCompany crc = em.find(CarRentalCompany.class, quote.getRentalCompany());
                Reservation res = crc.confirmQuote(quote);
                em.persist(res);
                done.add(res);
            }
        } catch (Exception e) {
            context.setRollbackOnly();
            for (Reservation r : done) {
                CarRentalCompany crc = em.find(CarRentalCompany.class, r.getRentalCompany());
                crc.cancelReservation(r);
            }
            throw new ReservationException(e);
        }
        return done;
    }

    @Override
    public void setRenterName(String name) {
        if (renter != null) {
            throw new IllegalStateException("name already set");
        }
        renter = name;
    }
}
