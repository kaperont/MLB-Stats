package dataaccesslayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.spi.Stoppable;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import bo.Player;
import bo.Team;
import bo.TeamSeason;

public class HibernateUtil {

	private static final SessionFactory sessionFactory;

	static {
		try {
			Configuration cfg = new Configuration()
				.addAnnotatedClass(bo.Player.class)
				.addAnnotatedClass(bo.PlayerSeason.class)
				.addAnnotatedClass(bo.Team.class)
				.addAnnotatedClass(bo.TeamSeason.class)
				.addAnnotatedClass(bo.BattingStats.class)
				.addAnnotatedClass(bo.CatchingStats.class)
				.addAnnotatedClass(bo.FieldingStats.class)
				.addAnnotatedClass(bo.PitchingStats.class)
				.configure();
			StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().
			applySettings(cfg.getProperties());
			sessionFactory = cfg.buildSessionFactory(builder.build());
		} catch (Throwable ex) {
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
  
  public static void stopConnectionProvider() {
    final SessionFactoryImplementor sessionFactoryImplementor = (SessionFactoryImplementor) sessionFactory;
    ConnectionProvider connectionProvider = sessionFactoryImplementor.getConnectionProvider();
    if (Stoppable.class.isInstance(connectionProvider)) {
        ((Stoppable) connectionProvider).stop();
    }        
}
	
	public static Player retrievePlayerById(Integer id) {
        Player p=null;
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		try {
			tx.begin();
			org.hibernate.Query query;
			query = session.createQuery("from bo.Player where id = :id ");
		    query.setParameter("id", id);
		    if (query.list().size()>0) {
		    	p = (Player) query.list().get(0);
		    	Hibernate.initialize(p.getTeamSeasons());
		    }
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			if (session.isOpen()) session.close();
		}
		return p;
	}
	
	
	@SuppressWarnings("unchecked")
	public static List<Player> retrievePlayersByName(String nameQuery, Boolean exactMatch) {
        List<Player> list=null;
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		try {
			tx.begin();
			org.hibernate.Query query;
			if (exactMatch) {
				query = session.createQuery("from bo.Player where name = :name ");
			} else {
				query = session.createQuery("from bo.Player where name like '%' + :name + '%' ");
			}
		    query.setParameter("name", nameQuery);
		    list = query.list();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			if (session.isOpen()) session.close();
		}
		return list;
	}
	
	public static boolean persistPlayer(Player p) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		try {
			tx.begin();
			session.saveOrUpdate(p);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
			return false;
		} finally {
			if (session.isOpen()) session.close();
		}
		return true;
	}
	
	public static boolean persistTeam(Team t) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		try {
			tx.begin();
			session.save(t);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
			return false;
		} finally {
			if (session.isOpen()) session.close();
		}
		return true;
	}
	
	public static boolean flushObjects() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		try {
			session.flush();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (session.isOpen()) session.close();
		}
		return true;
	}

	// Grab Teams by name
	@SuppressWarnings("unchecked")
	public static List<Team> retrieveTeamsByName(String nameQuery, Boolean exactMatch) {
        List<Team> list=null;
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		try {
			tx.begin();
			org.hibernate.Query query;
			if (exactMatch) {
				query = session.createQuery("from bo.Team where name = :name ");
			} else {
				query = session.createQuery("from bo.Team where name like '%' + :name + '%' ");
			}
		    query.setParameter("name", nameQuery);
		    list = query.list();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			if (session.isOpen()) session.close();
		}
		return list;
	}

	// Grab Teams by ID
	public static Team retrieveTeamById(Integer id) {
        Team t=null;
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		try {
			tx.begin();
			org.hibernate.Query query;
			query = session.createQuery("from bo.Team where id = :id ");
		    query.setParameter("id", id);
		    if (query.list().size()>0) {
		    	t = (Team) query.list().get(0);
		    	Hibernate.initialize(t.getSeasons());
		    }
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			if (session.isOpen()) session.close();
		}
		return t;
	}

	// retrieveTeamSeasonById(Integer teamId, Integer year)	
	public static TeamSeason retrieveTeamSeasonById(Integer teamId, Integer year) {
        TeamSeason ts=null;

		Team t = retrieveTeamById(teamId);
		Set<TeamSeason> seasons = t.getSeasons();
        List<TeamSeason> list = new ArrayList<TeamSeason>(seasons);
        Collections.sort(list, TeamSeason.teamSeasonsComparator);

        for (TeamSeason tSeason: list) {
            int idYear = tSeason.getYear();
            if (idYear == year) {
                ts = tSeason;
                break;
            }
		}
        
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		try {
			tx.begin();
			org.hibernate.Query query;
			query = session.createQuery("from bo.TeamSeason where id = :id ");
		    query.setParameter("id", ts.getId());
		    if (query.list().size()>0) {
		    	ts = (TeamSeason) query.list().get(0);
		    	Hibernate.initialize(ts.getPlayers());
		    }
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			if (session.isOpen()) session.close();
		}
		return ts;
	}
}