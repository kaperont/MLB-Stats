package bo;

import java.io.Serializable;
import java.util.Comparator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@SuppressWarnings("serial")
@Entity(name = "teamseason")
public class TeamSeason implements Serializable {

    @EmbeddedId
    PlayerSeasonId id;

    @Embeddable
    static class TeamSeasonId implements Serializable {
        @ManyToOne
		@JoinColumn(name = "teamid", referencedColumnName = "teamid", insertable = false, updatable = false)
		Team team;
		@Column(name="year")
		Integer teamYear;

		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof TeamSeasonId)){
				return false;
			}
			TeamSeasonId other = (TeamSeasonId)obj;
			// in order for two different object of this type to be equal,
			// they must be for the same year and for the same player
			return (this.team==other.team &&
					this.teamYear==other.teamYear);
		}

        @Override
		public int hashCode() {
			Integer hash = 0;
			if (this.team != null) hash += this.team.hashCode();
			if (this.teamYear != null) hash += this.teamYear.hashCode();
			return hash;
		}
    }

    @Column
    int gamesPlayed;
    @Column
    int wins;
    @Column
    int losses;
    @Column
    int rank;
    @Column
    int totalAttendance;

    public TeamSeason() {}

    public TeamSeason(Team t, Integer year){
        TeamSeasonId tsi = new TeamSeasonId();
        tsi.team = t;
        tsi.teamYear = year;
        this.id = psi;
    }

    // GETTERS
    public Integer getYear() {
        return this.id.teamYear;
    }

    public Team getTeam(){
        return this.id.team;
    }

    public int getGamesPlayed(){
        return gamesPlayed;
    }

    public int getWins(){
        return wins;
    }

    public int getLosses(){
        return losses;
    }

    public int getRank(){
        return rank;
    }

    public int getTotalAttendance(){
        return totalAttendance;
    }


    // SETTERS
    public void setYear(Integer y){
        this.id.teamYear = y;
    }

    public void setTeam(Team t){
        this.id.team = t;
    }

    public void setGamesPlayed(int gp){
        gamesPlayed = gp;
    }

    public void setWins(int w){
        wins = w;
    }

    public void setLosses(int l){
        losses = l;
    }

    public void setRank(int r){
        rank = r;
    }

    public void setTotalAttendance(int ta){
        totalAttendance = ta;
    }


    @Override
	public boolean equals(Object obj) {
		if(!(obj instanceof TeamSeason)){
			return false;
		}
		TeamSeason other = (TeamSeason)obj;
		return other.getId().equals(this.getId());
	}
	 
	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

	public static Comparator<TeamSeason> playerSeasonsComparator = new Comparator<TeamSeason>() {

		public int compare(TeamSeason ps1, TeamSeason ps2) {
			Integer year1 = ps1.getYear();
			Integer year2 = ps2.getYear();
			return year1.compareTo(year2);
		}

	};

}