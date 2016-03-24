package springbook.issuetracker.sqlservice;

import springbook.user.sqlservice.updatable.ConcurrentHashMapSqlRegistry;

public class ConcurrentHashMapSqlRegistryTest extends AbstractUpdatableSqlRegistryTest {

	@Override
	protected UpdatableSqlRegistry createUpdatableSqlRegistry() {
		return new ConcurrentHashMapSqlRegistry();
	}

}
