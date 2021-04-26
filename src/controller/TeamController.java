package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import view.TeamView;
import bo.Player;
import bo.PlayerSeason;
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
        int year = Integer.parseInt(id.substring(0, 4));

//        if (id == null) {
//            return;
//        }

        TeamSeason ts = (TeamSeason) HibernateUtil.retrieveTeamSeasonById(teamId, year);
        if (ts == null) return;
        buildSearchResultsTableTeamRoster(ts);
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
            table[i + 1][0] = tid;
            table[i + 1][1] = view.encodeLink(new String[]{"id"}, new String[]{tid}, t.getName(), ACT_DETAIL, SSP_TEAM);
            table[i + 1][2] = t.getLeague();
            table[i + 1][3] = t.getYearFounded().toString();
            table[i + 1][4] = t.getYearLast().toString();
        }

        view.appendScrollBeginning();
        view.buildTable(table);
        view.appendScrollEnd();
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
            seasonTable[i][1] = view.encodeLink(new String[]{"id"}, new String[]{insert}, "Roster", ACT_ROSTER, SSP_TEAM);
        	seasonTable[i][2] = ts.getGamesPlayed().toString();
        	seasonTable[i][3] = ts.getWins().toString();
        	seasonTable[i][4] = ts.getLosses().toString();
        	seasonTable[i][5] = ts.getRank().toString();
        	seasonTable[i][6] = ts.getTotalAttendance().toString();
        }
        
        view.appendScrollBeginning();
        view.buildTable(teamTable);
        view.buildTable(seasonTable);
        view.appendScrollEnd();
    }


    private void buildSearchResultsTableTeamRoster(TeamSeason ts) {
        Team t = ts.getTeam();
        int year = ts.getYear();
        String printYear = ts.getYear().toString();

        String[][] teamTable = new String[2][4];
        teamTable[0][0] = "Name";
        teamTable[0][1] = "League";
        teamTable[0][2] = "Year";
        teamTable[0][3] = "Player Payroll";
        teamTable[1][0] = t.getName();
        teamTable[1][1] = t.getLeague();
        teamTable[1][2] = printYear;

        Set<Player> players = ts.getPlayers();

        if(!players.isEmpty()){

            List<Player> playerList = new ArrayList<Player>(players);
            Collections.sort(playerList, Player.playerComparator);

            String[][] rosterTable = new String[playerList.size()+1][3];
            rosterTable[0][0] = "Name";
            rosterTable[0][1] = "Games Played";
            rosterTable[0][2] = "Salary";

            int i = 1;
            int playerPayroll = 0;

            for (Player p: playerList) {

                Set<PlayerSeason> psSet = p.getSeasons();

                if(!psSet.isEmpty()){
                    List<PlayerSeason> psList = new ArrayList<PlayerSeason>(psSet);
                    Collections.sort(psList, PlayerSeason.playerSeasonsComparator);

                    for(PlayerSeason ps: psList){
                        int psYear = ps.getYear();

                        if(psYear == year){
                            playerPayroll += ps.getSalary();
                            rosterTable[i][0] = p.getName();
                            rosterTable[i][1] = ps.getGamesPlayed().toString();
                            rosterTable[i][2] = DOLLAR_FORMAT.format(ps.getSalary());
                            i++;
                            break;
                        }
                    }
                }
            }

            teamTable[1][3] = DOLLAR_FORMAT.format(playerPayroll);

            view.appendScrollBeginning();
            view.buildTable(teamTable);
            view.buildTable(rosterTable);
            view.appendScrollEnd();
        }
    }
}