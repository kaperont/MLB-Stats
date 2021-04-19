package conversion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.sql.SQLException;

import bo.BattingStats;
import bo.CatchingStats;
import bo.FieldingStats;
import bo.PitchingStats;
import bo.Player;
import bo.PlayerSeason;
import bo.Team;
import bo.TeamSeason;
import dataaccesslayer.HibernateUtil;

public class Convert {

	// Define Connection Details (to LAMP VM)
	static Connection conn;
	static final String MYSQL_CONN_URL = "jdbc:mysql://163.11.236.96/mlb?"
    + "verifyServerCertificate=false&useSSL=true&"
    + "useLegacyDatetimeCode=false&serverTimezone=America/New_York&"
    + "user=root&password=password";

	public static void main(String[] args) {
		try {
			// Establish Connection to LAMP Server and convert data to MSSQL ORM Format
			long startTime = System.currentTimeMillis();
			conn = DriverManager.getConnection(MYSQL_CONN_URL);
			convert();
			long endTime = System.currentTimeMillis();
			long elapsed = (endTime - startTime) / (1000*60);
			System.out.println("Elapsed time in mins: " + elapsed);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (!conn.isClosed()) conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    HibernateUtil.stopConnectionProvider();
		HibernateUtil.getSessionFactory().close();
	}

	private static void convert() {
		try {

			// Retrieve data from LAMP Server
			HashMap<String,Player> players = getPlayers();
			System.out.println("Players Retrieved.");
			HashMap<String, Team> teams = getTeams();
			System.out.println("Teams Retrieved.");
			addTeamSeasons(teams);
			System.out.println("TeamSeasons Retrieved.");
			addPositions(players);
			System.out.println("Positions Retrieved.");
			addSeasons(players, teams);
			System.out.println("Seasons Retrieved.");

			// Persist data to Windows VM (MSSQL) using HibernateUtil
			// Persist Players
			for (Player p : players.values()) {
				HibernateUtil.persistPlayer(p);
			}
			System.out.println("Persisted Players.");
			
			// Persist Teams.
			for (Team t : teams.values()) {
				HibernateUtil.persistTeam(t);
			}
			System.out.println("Persisted teams.");

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Retrieve Teams from LAMP Server
	public static HashMap<String, Team> getTeams() throws SQLException {

		// Define a Team hashmap to throw Team data into
		HashMap<String, Team> teams = new HashMap<String, Team>();

		// Create a prepared statement and execute
		PreparedStatement ps = conn.prepareStatement("select distinct " +
				"teamID, name, lgID " +
				"from Teams");
		ResultSet rs = ps.executeQuery();
		
		// Sort through the data
		while (rs.next()) {

			String team = rs.getString("teamID");
			String teamName = rs.getString("name");
			String league = rs.getString("lgID");
			int yearFounded = 0;
			int yearLast = 0;

			if (team == null	|| team.isEmpty() 
								|| teamName == null 
								|| teamName.isEmpty())
				continue;

			// Create a new prepared statement and execute in order to find the yearFounded for a given team
			PreparedStatement ps1 = conn.prepareStatement("select " +
				"min(yearID) as year " +
				"from Teams " +
				"where teamID=\'" + team + "\'");
			ResultSet rs1 = ps1.executeQuery();
			while (rs1.next()) {
				yearFounded = rs1.getInt("year");
			}

			// Close above connection
			rs1.close();
			ps1.close();

			// Create a new prepared statement and execute in order to find the yearLast for a given team
			PreparedStatement ps2 = conn.prepareStatement("select " +
				"max(yearID) as year " +
				"from Teams " +
				"where teamID=\'" + team + "\'");
			ResultSet rs2 = ps2.executeQuery();
			while (rs2.next()) {
				yearLast = rs2.getInt("year");
			}

			// Close above connection
			rs2.close();
			ps2.close();

			// Create a new Team object and push it into the hashmap
			Team t = new Team();
			t.setName(teamName);
			t.setLeague(league);
			t.setYearFounded(yearFounded);
			t.setYearLast(yearLast);

			teams.put(team, t);
		}

		// Close Connection
		rs.close();
		ps.close();

		return teams;
	}
	
	// Add Seasons to each Team object stored
	private static void addTeamSeasons(HashMap<String, Team> teams) throws SQLException {
        try {

			// Create a prepared statement and execute
			PreparedStatement ps = conn.prepareStatement("select " + 
					"teamID, yearID, sum(G) as gamesPlayed, sum(W) as wins, sum(L) as losses, Rank, attendance " +
					"from Teams " +
					"group by teamID, yearID, Rank, attendance");
			ResultSet rs = ps.executeQuery();

			// Sort through the data
			while (rs.next()) {
				String team = rs.getString("teamID");
				int yid = rs.getInt("yearID");
				int games = rs.getInt("gamesPlayed");
				int win = rs.getInt("wins");
				int loss = rs.getInt("losses");
				int rank = rs.getInt("Rank");
				int att = rs.getInt("attendance");

				// Grab the team related to the current season being looked at
				Team t = teams.get(team);
				if (t != null) {
					TeamSeason s = new TeamSeason(t, yid);
					s.setYear(yid);
					s.setGamesPlayed(games);
					s.setWins(win);
					s.setLosses(loss);
					s.setRank(rank);
					s.setTotalAttendance(att);
					
					// Add the season to the team
					t.addSeason(s);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Retrieve Players from LAMP Server
	public static HashMap<String, Player> getPlayers() throws SQLException {
		HashMap<String, Player> players = new HashMap<String, Player>();
		PreparedStatement ps = conn.prepareStatement("select " + 
				"playerID, " + 
				"nameFirst, " + 
				"nameLast, " + 
				"nameGiven, "+ 
				"birthDay, " + 
				"birthMonth, " + 
				"birthYear, " + 
				"deathDay, "+ 
				"deathMonth, " + 
				"deathYear, " + 
				"bats, " + 
				"throws, " + 
				"birthCity, " + 
				"birthState, " + 
				"birthCountry, " + 
				"debut, " + 
				"finalGame " +
				"from Master");
		// for debugging comment previous line, uncomment next line
		//"from Master where playerID = 'bondsba01' or playerID = 'youklke01';");
		ResultSet rs = ps.executeQuery();
		int count=0; // for progress feedback only
		while (rs.next()) {
			count++;
			// this just gives us some progress feedback
			if (count % 1000 == 0)
				System.out.println("num players: " + count);

			String pid = rs.getString("playerID");
			String firstName = rs.getString("nameFirst");
			String lastName = rs.getString("nameLast");
			// this check is for data scrubbing
			// don't want to bring anybody over that doesn't have a pid, firstname and lastname
			if (pid == null	|| pid.isEmpty() 
							|| firstName == null 
							|| firstName.isEmpty() 
							|| lastName == null 
							|| lastName.isEmpty()) 
				continue;
			Player p = new Player();
			p.setName(firstName + " " + lastName);
			p.setGivenName(rs.getString("nameGiven"));

			java.util.Date birthDay = convertIntsToDate(rs.getInt("birthYear"), rs.getInt("birthMonth"), rs.getInt("birthDay"));
			if (birthDay!=null) 
				p.setBirthDay(birthDay);
			
			java.util.Date deathDay = convertIntsToDate(rs.getInt("deathYear"), rs.getInt("deathMonth"), rs.getInt("deathDay"));
			if (deathDay!=null)
				p.setDeathDay(deathDay);

			// need to do some data scrubbing for bats and throws columns
			String hand = rs.getString("bats");
			if (hand!=null){
				if (hand.equalsIgnoreCase("B")){
					hand = "S";
				}
				else if (hand.equalsIgnoreCase(""))
					hand = null;
			} 
			p.setBattingHand(hand);

			// Clean up throwing hand
			hand = rs.getString("throws");
			if (hand.equalsIgnoreCase("")){
				hand = null;
			} 
			p.setThrowingHand(hand);

			p.setBirthCity(rs.getString("birthCity"));
			p.setBirthState(rs.getString("birthState"));
			p.setBirthCountry(rs.getString("birthCountry"));

			// Clean up debut and final game data.
			try {
				java.util.Date firstGame = rs.getDate("debut");
				if (firstGame!=null) 
					p.setFirstGame(firstGame);
			}
			catch (SQLException e){
				// Ignore conversion error - remains null;
				System.out.println(pid + ": debut invalid format");
			}
			try {
				java.util.Date lastGame = rs.getDate("finalGame");
				if (lastGame!=null)
					p.setLastGame(lastGame);
			}
			catch (SQLException e){
				// Ignore conversion error - remains null
				System.out.println(pid + ": finalGame invalid format");
			}

			players.put(pid, p);
		}
		rs.close();
		ps.close();
		return players;
	}
	
	private static java.util.Date convertIntsToDate(int year, int month, int day) {
		Calendar c = new GregorianCalendar();
		java.util.Date d=null;
		// if year is 0, then date wasn't populated in MySQL database
		if (year!=0) {
			c.set(year, month-1, day);
			d = c.getTime();
		}
		return d;
	}
	
	// Add Positions to Players
	public static void addPositions(HashMap<String, Player> players) {
		try {
			PreparedStatement ps = conn.prepareStatement("select " +
					"distinct playerID, pos from Fielding");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String pid = rs.getString("playerID");
				String pos = rs.getString("pos");
				if (players.containsKey(pid)) {
					players.get(pid).addPosition(pos);
				}
			}
			rs.close();
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Add Seasons to Players and add Players to TeamSeasons
	public static void addSeasons(HashMap<String, Player> players, HashMap<String, Team> teams) {
		try {

			// Create a prepared statement and execute
			PreparedStatement ps = conn.prepareStatement("select " + 
					"playerID, yearID, teamID, lgID, sum(G) as gamesPlayed " + 
					"from Batting " + 
					"group by playerID, yearID, teamID, lgID;");
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				int yid = rs.getInt("yearID");
				String pid = rs.getString("playerID");
				Player p = players.get(pid);
				String tid = rs.getString("teamID");
				Team t = teams.get(tid);
				if (p != null) {
					PlayerSeason s = p.getPlayerSeason(yid);
					TeamSeason ts = t.getTeamSeason(t);

					// it is possible to see more than one of these per player if he switched teams
					// set all of these attrs the first time we see this playerseason
					if (s == null) {
						s = new PlayerSeason(p,yid);
						s.setGamesPlayed(rs.getInt("gamesPlayed"));	
						s.setPlayer(p);
						p.addSeason(s);
					}
					else {
						s.setGamesPlayed(rs.getInt("gamesPlayed") + s.getGamesPlayed());
						s.setPlayer(p);
					}

					// Add players to teamseason
					if(ts != null){
						ts.addPlayer(p);
					}
					
				}
			}

			// Add other related data to players
			System.out.println("PlayerSeasons Retrieved.");
			addSalaries(players);
			System.out.println("Salaries Retrieved.");
			addBattingStats(players);		
			System.out.println("BattingStats Retrieved.");
			addFieldingStats(players);
			System.out.println("FieldingStats Retrieved.");
			addPitchingStats(players);
			System.out.println("PitchingStats Retrieved.");
			addCatchingStats(players);
			System.out.println("CatchingStats Retrieved.");
				
			// Close Connection
			rs.close();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Add Salaries to Players
	public static double addSalaries(HashMap<String, Player> players) {
		double salary = 0;
		try {
			PreparedStatement ps = conn.prepareStatement("select " + 
					"playerID, yearID, sum(salary) as salary " + 
					"from Salaries " +
					"group by playerID, yearID");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String pid = rs.getString("playerID");
				int yid = rs.getInt("yearID");
				salary = rs.getDouble("salary");
				Player p = players.get(pid);
				if (p != null ) {
					PlayerSeason psi = p.getPlayerSeason(yid);
					if (psi != null) {
						psi.setSalary(salary);
					}
				}
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return salary;
	}

	// Add Batting Stats to Players
	public static void addBattingStats(HashMap<String, Player> players) {
		try {
			PreparedStatement ps = conn.prepareStatement("select "	+
					"playerID, yearID, " +
					"sum(AB) as atBats, " + 
					"sum(H) as hits, " + 
					"sum(2B) as doubles, " + 
					"sum(3B) as triples, " + 
					"sum(HR) as homeRuns, " + 
					"sum(RBI) as runsBattedIn, " + 
					"sum(SO) as strikeouts, " + 
					"sum(BB) as walks, " + 
					"sum(HBP) as hitByPitch, " + 
					"sum(IBB) as intentionalWalks, " + 
					"sum(SB) as steals, " + 
					"sum(CS) as stealsAttempted " + 
					"from Batting " + 
					"group by playerID, yearID");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String pid = rs.getString("playerID");
				int yid = rs.getInt("yearID");
				Player p = players.get(pid);
				if (p != null) {
					PlayerSeason psi = p.getPlayerSeason(yid);
					if (psi != null) {
						BattingStats s = new BattingStats();
						s.setId(psi);
						s.setAtBats(rs.getInt("atBats"));
						s.setHits(rs.getInt("hits"));
						s.setDoubles(rs.getInt("doubles"));
						s.setTriples(rs.getInt("triples"));
						s.setHomeRuns(rs.getInt("homeRuns"));
						s.setRunsBattedIn(rs.getInt("runsBattedIn"));
						s.setStrikeouts(rs.getInt("strikeouts"));
						s.setWalks(rs.getInt("walks"));
						s.setHitByPitch(rs.getInt("hitByPitch"));
						s.setIntentionalWalks(rs.getInt("intentionalWalks"));
						s.setSteals(rs.getInt("steals"));
						s.setStealsAttempted(rs.getInt("stealsAttempted"));
						psi.setBattingStats(s);
					}
				}	
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Add Fielding Stats to Players
	public static void addFieldingStats(HashMap<String, Player> players) {
		try {
			PreparedStatement ps = conn.prepareStatement("select " +
					"playerID, yearID, " +
					"sum(E) as errors, " +
					"sum(PO) as putOuts " +
					"from Fielding " +
					"group by playerID, yearID");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String pid = rs.getString("playerID");
				int yid = rs.getInt("yearID");
				Player p = players.get(pid);
				if (p != null) {
					PlayerSeason psi = p.getPlayerSeason(yid);
					if (psi != null) {
						FieldingStats s = new FieldingStats();
						s.setId(psi);
						s.setErrors(rs.getInt("errors"));
						s.setPutOuts(rs.getInt("putOuts"));
						psi.setFieldingStats(s);
					}
				}
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Add Pitching Stats to Players
	public static void addPitchingStats(HashMap<String, Player> players) {
		try {
			PreparedStatement ps = conn.prepareStatement("select " +
					"playerID, yearID, " +
					"sum(IPOuts) as outsPitched, " + 
					"sum(ER) as earnedRunsAllowed, " +
					"sum(HR) as homeRunsAllowed, " + 
					"sum(SO) as strikeouts, " +
					"sum(BB) as walks, " + 
					"sum(W) as wins, " +
					"sum(L) as losses, " + 
					"sum(WP) as wildPitches, " +
					"sum(BFP) as battersFaced, " + 
					"sum(HBP) as hitBatters, " +
					"sum(SV) as saves " + 
					"from Pitching " +
					"group by playerID, yearID");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String pid = rs.getString("playerID");
				int yid = rs.getInt("yearID");
				Player p = players.get(pid);
				if (p != null) {
					PlayerSeason psi = p.getPlayerSeason(yid);
					if (psi != null) {
						PitchingStats s = new PitchingStats();
						s.setId(psi);
						s.setOutsPitched(rs.getInt("outsPitched"));
						s.setEarnedRunsAllowed(rs.getInt("earnedRunsAllowed"));
						s.setHomeRunsAllowed(rs.getInt("homeRunsAllowed"));
						s.setStrikeouts(rs.getInt("strikeouts"));
						s.setWalks(rs.getInt("walks"));
						s.setWins(rs.getInt("wins"));
						s.setLosses(rs.getInt("losses"));
						s.setWildPitches(rs.getInt("wildPitches"));
						s.setBattersFaced(rs.getInt("battersFaced"));
						s.setHitBatters(rs.getInt("hitBatters"));
						s.setSaves(rs.getInt("saves"));
						psi.setPitchingStats(s);
					}
				}
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Add Catching Stats to Players
	public static void addCatchingStats(HashMap<String, Player> players) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("select " +
					"playerID, yearID, " +
					"sum(PB) as passedBalls, " +
					"sum(WP) as wildPitches, " +
					"sum(SB) as stealsAllowed, " +
					"sum(CS) as stealsCaught " +
					"from Fielding " +
					"group by playerID, yearID");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String pid = rs.getString("playerID");
				int yid = rs.getInt("yearID");
				Player p = players.get(pid);
				if (p != null) {
					PlayerSeason psi = p.getPlayerSeason(yid);
					if (psi != null) {
						CatchingStats s = new CatchingStats();
						s.setId(psi);
						s.setPassedBalls(rs.getInt("passedBalls"));
						s.setWildPitches(rs.getInt("wildPitches"));
						s.setStealsAllowed(rs.getInt("stealsAllowed"));
						s.setStealsCaught(rs.getInt("stealsCaught"));
						psi.setCatchingStats(s);
					}
				}
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			ps.toString();
			e.printStackTrace();
		}
	}
}