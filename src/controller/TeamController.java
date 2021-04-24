package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import view.TeamView;
import bo.Player;
import bo.Team;
import bo.TeamSeason;
import dataaccesslayer.HibernateUtil;

public class TeamController extends BaseController {
    
    @Override
    public void init(String query) {
        System.out.println("building dynamic html for player");
        view = new TeamView();
        process(query);
    }

    @Override
    protected void performAction() {
        String action = keyVals.get("action");
        System.out.println("teamcontroller performing action: " + action);
        if (action.equalsIgnoreCase(ACT_SEARCHFORM)) {
            processSearchForm();
        } else if (action.equalsIgnoreCase(ACT_SEARCH)) {
            processSearch();
        } else if (action.equalsIgnoreCase(ACT_DETAIL)) {
            processDetails();
        } else if (action.equalsIgnoreCase(ACT_ROSTER)){
            processRoster();
        }
    }

    protected void processSearchForm() {
        view.buildSearchForm();
    }

    protected final void processSearch() {
        String name = keyVals.get("name");
        if (name == null) {
            return;
        }
        String v = keyVals.get("exact");
        boolean exact = (v != null && v.equalsIgnoreCase("on"));
        List<Team> bos = HibernateUtil.retrieveTeamsByName(name, exact);
        view.printSearchResultsMessage(name, exact);
        buildSearchResultsTablePlayer(bos);
        view.buildLinkToSearch();
    }

    protected final void processDetails() {
        String id = keyVals.get("id");
        if (id == null) {
            return;
        }
        Team t = (Team) HibernateUtil.retrieveTeamById(Integer.valueOf(id));
        if (t == null) return;
        buildSearchResultsTableTeamDetail(t);
        view.buildLinkToSearch();
    }

    protected final void processRoster() {
        String id = keyVals.get("id");
        int teamId = Integer.parseInt(id.substring(5));
        String year = id.substring(0, 4);

        if (id == null) {
            return;
        }

        Team t = (Team) HibernateUtil.retrieveTeamById(teamId);
        if (t == null) return;
        buildSearchResultsTableTeamRoster(t, year);
        view.buildLinkToSearch();
    }

    private void buildSearchResultsTablePlayer(List<Team> bos) {
        // need a row for the table headers
        String[][] table = new String[bos.size() + 1][5];
        table[0][0] = "Id";
        table[0][1] = "Name";
        table[0][2] = "League";
        table[0][3] = "Year Founded";
        table[0][4] = "Last Year Existed";
        for (int i = 0; i < bos.size(); i++) {
            Team t = bos.get(i);
            String tid = t.getId().toString();
            table[i + 1][0] = view.encodeLink(new String[]{"id"}, new String[]{tid}, tid, ACT_DETAIL, SSP_TEAM);
            table[i + 1][1] = t.getName();
            table[i + 1][2] = t.getLeague();
            table[i + 1][3] = t.getYearFounded().toString();
            table[i + 1][4] = t.getYearLast().toString();
        }

        view.buildTable(table);
    }

    private void buildSearchResultsTableTeamDetail(Team t) {
    	Set<TeamSeason> seasons = t.getSeasons();
    	List<TeamSeason> list = new ArrayList<TeamSeason>(seasons);
    	Collections.sort(list, TeamSeason.teamSeasonsComparator);
    	// build 2 tables.  first the player details, then the season details
        // need a row for the table headers
        String[][] teamTable = new String[2][3];
        teamTable[0][0] = "Name";
        teamTable[0][1] = "Year Founded";
        teamTable[0][2] = "Last Year Played";
        teamTable[1][0] = t.getName();
        teamTable[1][1] = t.getYearFounded().toString();
        teamTable[1][2] = t.getYearLast().toString();
        view.buildTable(teamTable);

        String[][] seasonTable = new String[seasons.size()+1][7];
        seasonTable[0][0] = "Year";
        seasonTable[0][1] = "Roster";
        seasonTable[0][2] = "Games Played";
        seasonTable[0][3] = "Wins";
        seasonTable[0][4] = "Losses";
        seasonTable[0][5] = "Rank";
        seasonTable[0][6] = "Total Attendance";
        int i = 0;
        for (TeamSeason ts: list) {
            String idYear = ts.getYear().toString();
            String teamId = ts.getTeam().getId().toString();
            String insert = idYear + " " + teamId;

        	i++;
        	seasonTable[i][0] = ts.getYear().toString();
            seasonTable[i][1] = view.encodeLink(new String[]{"id"}, new String[]{insert}, "roster", ACT_ROSTER, SSP_TEAM);
        	seasonTable[i][2] = ts.getGamesPlayed().toString();
        	seasonTable[i][3] = ts.getWins().toString();
        	seasonTable[i][4] = ts.getLosses().toString();
        	seasonTable[i][5] = ts.getRank().toString();
        	seasonTable[i][6] = ts.getTotalAttendance().toString();
        }
        view.buildTable(seasonTable);
    }


    private void buildSearchResultsTableTeamRoster(Team t, String year) {
        Set<TeamSeason> seasons = t.getSeasons();
        List<TeamSeason> list = new ArrayList<TeamSeason>(seasons);
        Collections.sort(list, TeamSeason.teamSeasonsComparator);

        String[][] teamTable = new String[2][3];
        teamTable[0][0] = "Name";
        teamTable[0][1] = "League";
        teamTable[0][2] = "Year";
        teamTable[1][0] = t.getName();
        teamTable[1][1] = t.getLeague();
        teamTable[1][2] = year;
        view.buildTable(teamTable);

        TeamSeason tSeason = null;
        for (TeamSeason ts: list) {
            int idYear = ts.getYear();
            int intYear = Integer.parseInt(year);
            if (idYear == intYear) {
                tSeason = ts;
                break;
            }
        }

        Set<Player> players = tSeason.getPlayers(); //yoski broski my dudesssss 

        if(!players.isEmpty()){

            List<Player> playerList = new ArrayList<Player>(players);
            Collections.sort(playerList, Player.playerComparator);

            String[][] rosterTable = new String[seasons.size()+1][1];
            rosterTable[0][0] = "Name";
            rosterTable[0][1] = "Games Played";
            rosterTable[0][2] = "Salary";

            int i = 0;
            for (Player p: playerList) {
                rosterTable[i][0] = p.getName();
                rosterTable[i][1] = "24";
                rosterTable[i][2] = "1000000";

                i++;
            }

            view.buildTable(rosterTable);

        }

        
    }

}
