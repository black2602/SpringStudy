package springbook.user.sqlservice;

import java.util.HashMap;

public class HashMapSqlRegistry implements SqlRegistry {
	private HashMap<String, String> sqlMap = new HashMap<String, String>();
	
	@Override
	public void registerSql(String key, String sql) {
		sqlMap.put(key, sql);
	}

	@Override
	public String findSql(String key) throws SqlNotFoundException {
		String sql = sqlMap.get(key);
		if (sql == null) throw new SqlNotFoundException(key + "에 대한 SQL을 찾을 수 없습니다.");
		else return sql;
	}

}
