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
		if (sql == null) throw new SqlNotFoundException(key + "�� ���� SQL�� ã�� �� �����ϴ�.");
		else return sql;
	}

}
