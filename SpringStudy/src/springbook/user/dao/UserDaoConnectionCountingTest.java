package springbook.user.dao;

import java.sql.SQLException;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class UserDaoConnectionCountingTest {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CountingDaoFactory.class);
		UserDaoJdbc dao = context.getBean("userDao", UserDaoJdbc.class);
		
		CountingConnectionMaker ccm = context.getBean("connectionMaker", CountingConnectionMaker.class);
		System.out.println("Connection counter : " + ccm.getCounter());
	}
}
