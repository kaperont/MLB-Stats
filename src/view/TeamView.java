package view;

public class TeamView extends BaseView {

    protected String teamName = "";
    
    public TeamView() {
        title = "Team";
    }

    @Override
    public void buildSearchForm() {
        body.append("<form id=\"form\" action=\"");
        body.append(title.toLowerCase());
        body.append(".ssp\" method=\"get\">\r\n");
        body.append("<div id=\"form-header\">Looking for a " + title.toLowerCase() + "?</div>");
        body.append("<div id=\"match-header\">Exact Match?</div>");
        body.append("<input id=\"form-input\" spellcheck=\"false\" type=\"text\" placeholder=\"Enter " + title.toLowerCase() + "...\" size=\"20\" name=\"name\"><input id=\"form-match\" type=\"checkbox\" name=\"exact\">\r\n");
        body.append("<input type=\"hidden\" name=\"action\" value=\"search\">\r\n");
        body.append("<input id=\"form-submit\" type=\"submit\" value=\"Submit\">\r\n");
        body.append("</form>\r\n"); 
    }
}
