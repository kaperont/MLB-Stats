package bo;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = "team")
public class Team{
    @Id
    Integer TeamId;

    @Column
    String name;
    @Column
    String league;
    @Column
    String yearFounded;
    @Column
    String yearLast;


    // SETTERS
    public void setName(String n){
        name = n;
    }
    
    public void setLeague(String l){
        league = l;
    }

    public void setYearFounded(String yf){
        yearFounded = yf;
    }

    public void setYearLast(String yl){
        yearLast = yl;
    }


    // GETTERS
    public String getName(){
        return name;
    }

    public String getLeague(){
        return league;
    }

    public String getYearFounded(){
        return yearFounded;
    }

    public String getYearLast(){
        return yearLast;
    }

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Team)){
			return false;
		}
		Team other = (Team) obj;
		return (this.getName().equalsIgnoreCase(other.getName()) &&
				this.getYearFounded()==other.getYearFounded() &&
				this.getYearLast()==other.getYearLast());
	}

    @Override
	public int hashCode() {
		Integer hash = 0;
		if (this.getName()!=null) hash += this.getName().hashCode(); 
		if (this.getYearFounded()!=null) hash += this.getYearFounded().hashCode();
		if (this.getYearLast()!=null) hash += this.getYearLast().hashCode();
		return hash;
	}

}