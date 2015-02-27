import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;


/**
 * 
 */
@javax.persistence.Entity(name = "Skill")
@Table(name = "SKILL")
@NamedQueries({
    @NamedQuery(name = "Skill.findBySkill", query = "SELECT s\nFROM Skill s\nWHERE s.skill= :skill\nORDER BY s.persistenceId"),
    @NamedQuery(name = "Skill.find", query = "SELECT s\nFROM Skill s\nORDER BY s.persistenceId"),
    @NamedQuery(name = "Skill.findSkillsByEmployeePersistenceId", query = "SELECT skills_1 FROM Employee employee_0 JOIN employee_0.skills as skills_1 WHERE employee_0.persistenceId= :persistenceId")
})
public class Skill
    implements org.bonitasoft.engine.bdm.Entity
{

    @Id
    @GeneratedValue
    private Long persistenceId;
    @Version
    private Long persistenceVersion;
    @Column(name = "SKILL", nullable = true)
    private String skill;

    public Skill() {
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

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getSkill() {
        return skill;
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
        Skill other = ((Skill) obj);
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
        if (skill == null) {
            if (other.skill!= null) {
                return false;
            }
        } else {
            if (!skill.equals(other.skill)) {
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
        int skillCode = 0;
        if (skill!= null) {
            skillCode = skill.hashCode();
        }
        result = ((prime*result)+ skillCode);
        return result;
    }

}