package springbook.user.service;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static springbook.user.service.UserServiceImpl.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/test-applicationContext.xml")
public class UserServiceTest {
	@Autowired
	PlatformTransactionManager transactionManager;
	@Autowired
	UserService userService;
	@Autowired
	UserService testUserService;
	@Autowired
	UserDao userDao;
	@Autowired
	DataSource dataSource;
	@Autowired
	MailSender mailSender;
	@Autowired
	ApplicationContext context;
	
	List<User> users;	

	@Before
	public void setUp() {
		users = Arrays.asList(
				new User("bumjin", "�ڹ���", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER-1, 0, "bumjin@hanmail.net"),
				new User("joytouch", "����", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "joytouch@t.com"),
				new User("erwins", "�Ž���", "p3", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD-1, "erwins@t.com"),
				new User("madnite1", "�̻�ȣ", "p4", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD, "madnite1@gmail.com"),
				new User("green", "���α�", "p5", Level.GOLD, 100, Integer.MAX_VALUE, "green@t.com"));
	}
	
	@Test
	public void bean() {
		assertThat(this.userService, is(notNullValue()));
	}
	
	@Test
	@DirtiesContext
	public void upgradeLevels() throws Exception {
		UserServiceImpl userServiceImpl = new UserServiceImpl();
		
		MockUserDao mockUserDao = new MockUserDao(this.users);
		userServiceImpl.setUserDao(mockUserDao);
		
		MockMailSender mockMailSender = new MockMailSender();
		userServiceImpl.setMailSender(mockMailSender);
		
		userServiceImpl.upgradeLevels();
		
		List<User> updated = mockUserDao.getUpdated();
		assertThat(updated.size(), is(2));
		checkUserAndLevel(updated.get(0), "joytouch", Level.SILVER);
		checkUserAndLevel(updated.get(1), "madnite1", Level.GOLD);
		
		List<String> request = mockMailSender.getRequests();
		assertThat(request.size(), is(2));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
	}

	private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
		assertThat(updated.getId(), is(expectedId));
		assertThat(updated.getLevel(), is(expectedLevel));
	}

//	@Test
//	public void mockUpgradeLevels() throws Exception {
//		UserServiceImpl userServiceImpl = new UserServiceImpl();
//		
//		UserDao mockUserDao = mock(UserDao.class);
//		when(mockUserDao.getAll()).thenReturn(this.users);
//		userServiceImpl.setUserDao(mockUserDao);
//		
//		MailSender mockMailSender = mock(MailSender.class);
//		userServiceImpl.setMailSender(mockMailSender);
//		
//		userServiceImpl.upgradeLevels();
//		
//		verify(mockUserDao, times(2)).update(any(User.class));
//		verify(mockUserDao, times(2)).update(any(User.class));
//		verify(mockUserDao).update(users.get(1));
//		assertThat(users.get(1).getLevel(), is(Level.SILVER));
//		verify(mockUserDao).update(users.get(3));
//		assertThat(users.get(3).getLevel(), is(Level.GOLD));
//		
//		ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);
//		verify(mockMailSender, times(2)).send(mailMessageArg.capture());
//		List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
//		assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
//		assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));
//		
//	}
	
	private void checkLevelUpgraded(User user, boolean upgraded) {
		User userUpdate = userDao.get(user.getId());
		if(upgraded) {
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		} else {
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}
	
	@Test
	public void add() {
		userDao.deleteAll();
		
		User userWithLevel = users.get(4);
		User userWithoutLevel = users.get(0);
		userWithoutLevel.setLevel(null);
		
		userService.add(userWithLevel);
		userService.add(userWithoutLevel);
		
		User userWithLevelRead = userDao.get(userWithLevel.getId());
		User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());
		
		assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
		assertThat(userWithoutLevelRead.getLevel(), is(userWithoutLevel.getLevel()));
	}
	
	@Test
	public void upgradeAllOrNothing() throws Exception{
		userDao.deleteAll();
		for(User user : users)
			userDao.add(user);
		
		try {
			this.testUserService.upgradeLevels();
			fail("TestUserServiceException expected");
		} catch(TestUserServiceException e) {
			
		}
		
		checkLevelUpgraded(users.get(1), false);
	}
	
	@Test
	public void advisorAutoProxyCreate() {
		assertThat(testUserService, instanceOf(java.lang.reflect.Proxy.class));
	}
	
	static class TestUserServiceImpl extends UserServiceImpl {
		private String id = "madnite1";
		
		@Override
		protected void upgradeLevel(User user) {
			if(user.getId().equals(this.id)) 
				throw new TestUserServiceException();
			super.upgradeLevel(user);
		}
	}
	
	static class TestUserServiceException extends RuntimeException {
		
	}
	
	static class MockMailSender implements MailSender {
		private List<String> requests = new ArrayList<String>();
		
		public List<String> getRequests() {
			return requests;
		}
		
		@Override
		public void send(SimpleMailMessage mailMessage) throws MailException {
			requests.add(mailMessage.getTo()[0]);
		}

		@Override
		public void send(SimpleMailMessage... arg0) throws MailException {
			
		}
	}
	
	static class MockUserDao implements UserDao {
		private List<User> users;
		private List<User> updated = new ArrayList();
		
		private MockUserDao(List<User> users) {
			this.users = users;
		}
		
		public List<User> getUpdated() {
			return updated;
		}
		
		@Override
		public List<User> getAll() {
			return this.users;
		}
		

		@Override
		public void update(User user) {
			updated.add(user);
		}

		@Override
		public void add(User user) {
			throw new UnsupportedOperationException();			
		}

		@Override
		public User get(String id) {
			throw new UnsupportedOperationException();		
		}

		@Override
		public void deleteAll() {
			throw new UnsupportedOperationException();		
		}

		@Override
		public int getCount() {
			throw new UnsupportedOperationException();		
		}

		
	}
}	
