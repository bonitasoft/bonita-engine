import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Version;


/**
 * 
 */
@javax.persistence.Entity(name = "Forecast")
@Table(name = "FORECAST")
@NamedQueries({
    @NamedQuery(name = "Forecast.find", query = "SELECT f\nFROM Forecast f\nORDER BY f.persistenceId")
})
public class Forecast
    implements org.bonitasoft.engine.bdm.Entity
{

    @Id
    @GeneratedValue
    private Long persistenceId;
    @Version
    private Long persistenceVersion;
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    @Column(name = "TEMPERATURES", nullable = true)
    private List<Double> temperatures = new ArrayList<Double>(10);

    public Forecast() {
    }

    public void setPersistenceId(Long persistenceId) {
        this.persistenceId = persistenceId;
    }

    public Long getPersistenceId() {
        return persistenceId;
    }

    public void setPersistenceVersion(Long persistenceVersion) {
        this.persistenceVersion = persistenceVersion;
    }

    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    public void setTemperatures(List<Double> temperatures) {
        if (this.temperatures == null) {
            this.temperatures = temperatures;
        } else {
            this.temperatures.clear();
            this.temperatures.addAll(temperatures);
        }
    }

    public List<Double> getTemperatures() {
        return temperatures;
    }

    public void addToTemperatures(Double addTo) {
        List temperatures = getTemperatures();
        temperatures.add(addTo);
    }

    public void removeFromTemperatures(Double removeFrom) {
        List temperatures = getTemperatures();
        temperatures.remove(removeFrom);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass()!= obj.getClass()) {
            return false;
        }
        Forecast other = ((Forecast) obj);
        if (persistenceId == null) {
            if (other.persistenceId!= null) {
                return false;
            }
        } else {
            if (!persistenceId.equals(other.persistenceId)) {
                return false;
            }
        }
        if (persistenceVersion == null) {
            if (other.persistenceVersion!= null) {
                return false;
            }
        } else {
            if (!persistenceVersion.equals(other.persistenceVersion)) {
                return false;
            }
        }
        if (temperatures == null) {
            if (other.temperatures!= null) {
                return false;
            }
        } else {
            if (!temperatures.equals(other.temperatures)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        int persistenceIdCode = 0;
        if (persistenceId!= null) {
            persistenceIdCode = persistenceId.hashCode();
        }
        result = ((prime*result)+ persistenceIdCode);
        int persistenceVersionCode = 0;
        if (persistenceVersion!= null) {
            persistenceVersionCode = persistenceVersion.hashCode();
        }
        result = ((prime*result)+ persistenceVersionCode);
        int temperaturesCode = 0;
        if (temperatures!= null) {
            temperaturesCode = temperatures.hashCode();
        }
        result = ((prime*result)+ temperaturesCode);
        return result;
    }

}
