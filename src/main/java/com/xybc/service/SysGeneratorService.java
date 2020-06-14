package com.xybc.service;

import com.xybc.dao.SysGeneratorDao;
import com.xybc.dao.SysGeneratorOrcalDao;
import com.xybc.utils.GenUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

@Service
public class SysGeneratorService {
	@Autowired
	private SysGeneratorDao sysGeneratorDao;

	@Autowired
	private SysGeneratorOrcalDao sysGeneratorOrcalDao;

	@Autowired
	private Environment env;



	public List<Map<String, Object>> queryList(Map<String, Object> map) {
		String dbType = env.getProperty("spring.dbType" );
		if ("orcal".equals(dbType)){
			int limit = Integer.parseInt(map.get("limit").toString());
			int offset = Integer.parseInt(map.get("offset").toString());
			map.put("endSize",offset+limit);
			return sysGeneratorOrcalDao.queryList(map);
		}else {
			return sysGeneratorDao.queryList(map);
		}

	}

	public int queryTotal(Map<String, Object> map) {
		String dbType = env.getProperty("spring.dbType" );
		if ("orcal".equals(dbType)){
			return sysGeneratorOrcalDao.queryTotal(map);
		}else {
			return sysGeneratorDao.queryTotal(map);
		}

	}

	public Map<String, String> queryTable(String tableName) {
		String dbType = env.getProperty("spring.dbType" );
		if ("orcal".equals(dbType)){
			return sysGeneratorOrcalDao.queryTable(tableName);
		}else {
			return sysGeneratorDao.queryTable(tableName);
		}
	}


	public List<Map<String, String>> queryColumns(String tableName) {
		String dbType = env.getProperty("spring.dbType" );
		if ("orcal".equals(dbType)){
			return sysGeneratorOrcalDao.queryColumns(tableName);
		}else {
			return sysGeneratorDao.queryColumns(tableName);
		}
	}

	public byte[] generatorCode(String[] tableNames) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(outputStream);

		for(String tableName : tableNames){
			//查询表信息
			Map<String, String> table = queryTable(tableName);
			//查询列信息
			List<Map<String, String>> columns = queryColumns(tableName);
			//生成代码
			GenUtils.generatorCode(table, columns, zip);
		}
		IOUtils.closeQuietly(zip);
		return outputStream.toByteArray();
	}
}
