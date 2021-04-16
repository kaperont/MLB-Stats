package bo;

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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer TeamId;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "id.team")
    @Fetch(FetchMode.JOIN)
	Set<TeamSeason> seasons = new HashSet<TeamSeason>();

    @Column
    String name;
    @Column
    String league;
    @Column
    Integer yearFounded;
    @Column
    Integer yearLast;


    // SETTERS
    public void setName(String n){
        name = n;
    }
    
    public void setLeague(String l){
        league = l;
    }

    public void setYearFounded(Integer yf){
        yearFounded = yf;
    }

    public void setYearLast(Integer yl){
        yearLast = yl;
    }

    public void addSeason(TeamSeason s) {
        seasons.add(s);
    }

    public void setId(Integer id){
        this.TeamId = id;
    }


    // GETTERS

    public String getName(){
        return name;
    }

    public String getLeague(){
        return league;
    }

    public Integer getYearFounded(){
        return yearFounded;
    }

    public Integer getYearLast(){
        return yearLast;
    }

    public TeamSeason getTeamSeason(Team team) {
        for (TeamSeason ts : seasons) {
            if (ts.getTeam().equals(team)) return ts;
        }
        return null;
    }

    public Integer getId(){
        return this.TeamId;
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