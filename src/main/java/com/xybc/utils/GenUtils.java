package com.xybc.utils;

import com.xybc.entity.ColumnEntity;
import com.xybc.entity.TableEntity;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 工具类
 *
 */
public class GenUtils {

	public static List<String> getTemplates(){
		List<String> templates = new ArrayList<String>();
		templates.add("template/Entity.java.vm");
		//templates.add("template/Dao.java.vm");
		templates.add("template/Dao.xml.vm");
		templates.add("template/Service.java.vm");
		templates.add("template/ServiceImpl.java.vm");
		templates.add("template/Controller.java.vm");
		templates.add("template/list.html.vm");
		templates.add("template/detail.html.vm");
		templates.add("template/add.html.vm");
		templates.add("template/update.html.vm");

		//templates.add("template/list.js.vm");
		//templates.add("template/menu.sql.vm");
		return templates;
	}

	/**
	 * 生成代码
	 */
	public static void generatorCode(Map<String, String> table,
			List<Map<String, String>> columns, ZipOutputStream zip){
		//配置信息
		Configuration config = getConfig();
		//表信息
		TableEntity tableEntity = new TableEntity();
		tableEntity.setTableName(table.get("TABLE_NAME"));
		tableEntity.setComments(table.get("TABLE_COMMENT"));
		//表名转换成Java类名
		String className = tableToJava(tableEntity.getTableName(), config.getString("tablePrefix"));
		tableEntity.setClassName(className);
		tableEntity.setClassname(StringUtils.uncapitalize(className));

		//列信息
		List<ColumnEntity> columsList = new ArrayList<>();
		for(Map<String, String> column : columns){
			ColumnEntity columnEntity = new ColumnEntity();
			columnEntity.setColumnName(column.get("COLUMN_NAME"));
			columnEntity.setDataType(column.get("DATA_TYPE"));
			columnEntity.setComments(column.get("COLUMN_COMMENT"));
			columnEntity.setExtra(column.get("EXTRA"));

			//列名转换成Java属性名
			String attrName = columnToJava(columnEntity.getColumnName());
			columnEntity.setAttrName(attrName);
			columnEntity.setAttrname(StringUtils.uncapitalize(attrName));

			//列的数据类型，转换成Java类型
			String attrType = config.getString(columnEntity.getDataType().toLowerCase(), "unknowType");
			columnEntity.setAttrType(attrType);

			//是否主键
			if("PRI".equalsIgnoreCase(column.get("COLUMN_KEY")) && tableEntity.getPk() == null){
				tableEntity.setPk(columnEntity);
			}

			columsList.add(columnEntity);
		}
		tableEntity.setColumns(columsList);

		//没主键，则第一个字段为主键
		if(tableEntity.getPk() == null){
			tableEntity.setPk(tableEntity.getColumns().get(0));
		}

		//设置velocity资源加载器
		Properties prop = new Properties();
		prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		Velocity.init(prop);

		//封装模板数据
		Map<String, Object> map = new HashMap<>(20);
		map.put("tableName", tableEntity.getTableName());
		map.put("comments", tableEntity.getComments());
		map.put("pk", tableEntity.getPk());
		map.put("className", tableEntity.getClassName());
		map.put("classname", tableEntity.getClassname());
		map.put("pathName", tableEntity.getClassname().toLowerCase());
		map.put("columns", tableEntity.getColumns());
		map.put("package", config.getString("package"));
		map.put("author", config.getString("author"));
		map.put("email", config.getString("email"));
		map.put("basePath", config.getString("basePath"));
		String satp = config.getString("satp");
		map.put("satp", satp);
		satp=satp.substring(0,1).toUpperCase().concat(satp.substring(1));
		map.put("Satp", satp);
		map.put("satpPackage", config.getString("satpPackage"));
		map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        VelocityContext context = new VelocityContext(map);

        //获取模板列表
		List<String> templates = getTemplates();
		for(String template : templates){
			//渲染模板
			StringWriter sw = new StringWriter();
			Template tpl = Velocity.getTemplate(template, "UTF-8");
			tpl.merge(context, sw);

			try {
				//添加到zip
				zip.putNextEntry(new ZipEntry(getFileName(template, tableEntity.getClassName(), tableEntity.getClassname(), config.getString("package"))));
				IOUtils.write(sw.toString(), zip, "UTF-8");
				IOUtils.closeQuietly(sw);
				zip.closeEntry();
			} catch (IOException e) {
				throw new RRException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
			}
		}
	}


	/**
	 * 列名转换成Java属性名
	 */
	public static String columnToJava(String columnName) {
		return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
	}

	/**
	 * 表名转换成Java类名
	 */
	public static String tableToJava(String tableName, String tablePrefix) {
		if(StringUtils.isNotBlank(tablePrefix)){
			tableName = tableName.replace(tablePrefix, "");
		}

		//此处根据xybc表命名规范，将 t_sys_user 转为sys_user
		//int i = tableName.indexOf("_");
		// tableName = tableName.substring(i+1,tableName.length());
		return columnToJava(tableName);
	}

	/**
	 * 获取配置信息
	 */
	public static Configuration getConfig(){
		try {
			return new PropertiesConfiguration("generator.properties");
		} catch (ConfigurationException e) {
			throw new RRException("获取配置文件失败，", e);
		}
	}

	/**
	 * 获取文件名
	 */
	public static String getFileName(String template, String className, String classname, String packageName){
		String packagePath = "main" + File.separator + "java" + File.separator;
		/*if(StringUtils.isNotBlank(packageName)){
			packagePath += packageName.replace(".", File.separator) + File.separator;
		}
		*/
		if(template.contains("Entity.java.vm")){
			return packagePath + "entity" + File.separator + className + ".java";
		}
		
		/*if(template.contains("Dao.java.vm")){
			return packagePath + "dao" + File.separator + className + "Mapper.java";
		}*/

		if(template.contains("Service.java.vm")){
			return packagePath + "service" + File.separator +"Mg"+ className + "Service.java";
		}

		if(template.contains("ServiceImpl.java.vm")){
			return packagePath + "service" + File.separator + className + "Service.java";
		}

		if(template.contains("Controller.java.vm")){
			return packagePath + "controller" + File.separator + "Mg"+className + "Controller.java";
		}

		if(template.contains("Dao.xml.vm")){
            return packagePath + "mapper" + File.separator + className + "Mapper.xml";
		}

		if(template.contains("list.html.vm")){
			return "main" + File.separator + "resources" + File.separator + classname + File.separator
					+ classname + "List.jsp";
		}

		if(template.contains("add.html.vm")){
			return "main" + File.separator + "resources" + File.separator + classname + File.separator
					+ classname + "Add.jsp";
		}

		if(template.contains("update.html.vm")){
			return "main" + File.separator + "resources" + File.separator + classname + File.separator
					+ classname + "Update.jsp";
		}

		if(template.contains("detail.html.vm")){
			return "main" + File.separator + "resources" + File.separator + classname + File.separator
					+ classname + "Detail.jsp";
		}

		return null;
	}
}
