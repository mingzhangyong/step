## 数据导出到excel
示例方法
```java
public ResultInfo exportStationList(DeviceQueryParam param, int humanID) {
		if(items == null){
			items = getItems();
		}
		String sql = "select c01 as 中转站代码,c18 as 中转站名称, c02 as 区, c03 as 街道,c04 as 居委会 ,c05 as 地址,";
		sql = addCase2Sql(sql,items.get("SYQGSFL").get(0),items.get("SYQGSFL").get(1),"c06","所有权归属分类") + ",";
		sql += "c07 as 启用日期,c08 as 中转站造价（千元）,c09 as 中转站设备投资（千元）,";
		sql = addCase2Sql(sql,items.get("LJLX").get(0),items.get("LJLX").get(1),"c10","垃圾类型") + ",";
		sql+="c11 as 中转站占地面积（㎡）,c12 as '日均进量(吨)',c13 as '日均转运量(吨)',c14 as 中转站工作人员数,";
		sql = addCase2Sql(sql,items.get("LJYXFL").get(0),items.get("LJYXFL").get(1),"c15","垃圾运向分类");
		sql+=" from to_garbage_collection_station where 1=1 ";

		if (!StringUtils.isEmpty(param.getCode())) {
			sql += " and  C01 like '%"+param.getCode()+"%'";
		}
		if (!StringUtils.isEmpty(param.getArea())) {
			sql += " and C02 = '"+param.getArea()+"'";
		}
		if (!StringUtils.isEmpty(param.getStreet())) {
			sql += " and C03 = '"+param.getStreet()+"'";
		}
		if (!StringUtils.isEmpty(param.getName())) {
			sql += " and C18 like '%"+param.getName()+"%'";
		}

		List<Map<String, Object>> dataList = jdbcTemplate.queryForList(sql);
		if (ListUtil.isEmpty(dataList)) {
			ResultInfo result = new ResultInfo(false);
			result.setMessage("导出数据为空");
			return result;
		}
		return dataExportor.exportByListData(dataList, "中转站信息导出列表", humanID);
	}
```
- 过程：通过sql查询到List<Map<String,Object>>类型数据，然后通过dataExportor导出excel文件

具体的实现方式在                                            **dataExportor.exportByListData(dataList, "中转站信息导出列表", humanID)**   ;
	
	
	
	
	
	
	
#### 类dataExportor实现
```java
package cn.com.egova.export.service;

import cn.com.egova.ali.oss.dao.OSSManager;
import cn.com.egova.base.bean.ResultInfo;
import cn.com.egova.base.export.DataExportor;
import cn.com.egova.base.export.ExportDataProvider;
import cn.com.egova.base.export.SqlData;
import cn.com.egova.base.hibernate.HibernateTool;
import cn.com.egova.base.tools.JsonUtils;
import cn.com.egova.bizbase.constant.ConfigItemNameConst;
import cn.com.egova.bizbase.tools.HttpUtils;
import cn.com.egova.bizbase.tools.SysConfigUtils;
import cn.com.egova.export.bean.ExportConfig;
import cn.com.egova.export.bean.ViewColumn;
import cn.com.egova.export.bean.ViewData;
import cn.com.egova.export.dao.ExportTaskDao;
import cn.com.egova.export.mapping.ExpTaskInfo;
import cn.com.egova.export.service.imagehandler.ImageHandler;
import cn.com.egova.export.task.ServiceContext;
import cn.com.egova.export.task.TaskStatusMoniter;
import cn.com.egova.export.util.FileUtils;
import cn.com.egova.export.util.Tools;
import cn.com.egova.export.web.TaskStatusChanger;
import cn.com.egova.media.tools.FileStoreUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class DataExpManager implements DataExportor {
	
    private ExportConfig conf = null;
    
    @Autowired
	private ExcelExpTool excelTool = null;
	@Autowired
	private CsvExpTool csvTool = null;
	@Autowired
	private ExpTaskManager expTaskManager = null;
	@Autowired
	private ExportTaskDao exportTaskDao = null;
	@Autowired
	private ExportConfigManager exportConfigManager = null;

	@Autowired
	private JdbcTemplate jdbcTemplate = null;

	@Autowired
	private List<ImageHandler> handlers;

	@Autowired(required = false)
	private List<ExportDataProvider> dataProviders = null;

	private Map<String, ExportDataProvider> dataProviderMap = null;
	
	@Autowired
	OSSManager OSSManager = null;
	
	private static Logger logger = Logger.getLogger(DataExpManager.class);
 
    public void initConf() {
		conf = exportConfigManager.getConfig();
    }

	public ExportConfig getConf(){
		initConf();
		return conf;
	}

	private ExportDataProvider getProvider(String name){
		if(dataProviders != null){
			if(dataProviderMap == null){
				dataProviderMap = new HashMap<String, ExportDataProvider>();
				for (ExportDataProvider dataProvider : dataProviders) {
					dataProviderMap.put(dataProvider.getProviderName(), dataProvider);
				}
			}
			return dataProviderMap.get(name);
		}
		return null;
	}

	/**
	 * 下载已经存在的zip文件
	 * @param taskID
	 * @param request
	 * @param response
	 */
	public void downZipFile(int taskID,HttpServletRequest request, HttpServletResponse response) {
		initConf();
		String zipPath = expTaskManager.getZipURLByTask(taskID);
		String fileName = zipPath.substring(zipPath.lastIndexOf(File.separator) + 1);
		ResultInfo info = new ResultInfo(true);
		if(!zipPath.equals("notexist")){
			InputStream fileInputStream = null;
			OutputStream out = null;
			File f = new File(zipPath);
			try{
	            	response.reset();
	    			response.setContentType("application/octet-stream");
	    	        response.setHeader("Content-Disposition","attachment; filename=" + new String(fileName.getBytes("gb2312"),"ISO8859-1")); 
	    	        boolean IEVersion6_0 = (request.getHeader("User-Agent").indexOf("MSIE 6.0")>0); 
	    	        if(IEVersion6_0){ 	       
	    	           response.setHeader("Content-Disposition","filename=" +  new String(fileName.getBytes("gb2312"),"ISO8859-1"));
	    	        }
//	    	        Integer mediaStoreType = SysConfigUtils.getSysConfigIntValue(ConfigItemNameConst.COMMON_MEDIA_STORE_TYPE).intValue();
	    	        if(SysConfigUtils.getSysConfigBoolValue(ConfigItemNameConst.MIS_REC_UP_YUN_ENABLE)){
//	    	        	if(mediaStoreType == 3){
//							fileInputStream = FtpUtils.getFileInputStream(zipPath);
//						} else if(mediaStoreType == 1){
//							fileInputStream = FileUtils.getFileInputStream(zipPath);
//						} else {
//							fileInputStream = OSSManager.getObjectAsStream(zipPath);
//						}
						fileInputStream = FileStoreUtils.getFileInputStream(zipPath);
	    	        } else {
	    	        	fileInputStream = FileUtils.getInputStream(zipPath);
	    	        }
	    	        if( fileInputStream != null ){
		    	        byte[] buff = new byte[2048];
		    	        int bytesRead;
		    	        out = response.getOutputStream();
		    	        while(-1 != (bytesRead = fileInputStream.read(buff, 0, buff.length))) {
		    	        	out.write(buff, 0, bytesRead);
		    	        }
		    	        fileInputStream.close();
		    	        out.flush();
		    	        out.close();
		    	        logger.info("您导出的文件放置位置：" + zipPath);
	    	        }else{
	    	    		info.setSuccess(false);
	    				info.setMessage("下载文件过期被删除，请重新下载!");
	    	    		this.writeJsonResultToClient(response, info, 0);
	    	        }
	    	        //expTaskManager.moveTaskToHist(taskID);
			}catch(Exception ex){
				logger.error("下载包时发生错误!" + ex.getLocalizedMessage(), ex);
				info.setSuccess(false);
				info.setMessage("文件下载错误，请重新下载！");
			}finally{
		       try{
		    	   if(fileInputStream != null){ fileInputStream.close();};
			       if(out != null){out.close();};
		       }catch(Exception ex){
		    	   logger.error("关闭文件输出流时出错!" + ex.getLocalizedMessage());
		       }
				try{
					Integer mediaStoreType = SysConfigUtils.getSysConfigIntValue(ConfigItemNameConst.COMMON_MEDIA_STORE_TYPE).intValue();
					if(SysConfigUtils.getSysConfigBoolValue(ConfigItemNameConst.MIS_REC_UP_YUN_ENABLE)){
						if(mediaStoreType == 1){
							FileUtils.deleteShareFolder(zipPath.replace('\\', '/'));
						} else {
							FileStoreUtils.deleteFile(zipPath);
						}
//						if(mediaStoreType == 3){
//	    	        		FtpUtils.deleteFile(zipPath);
//						} else if(mediaStoreType == 1){
//							FileUtils.deleteShareFolder(zipPath.replace('\\', '/'));
//						} else {
//							fileInputStream = OSSManager.getObjectAsStream(zipPath);
//						}
					} else{
						if (f != null) {
							FileUtils.deleteFloder(f.getParent());
						}
					}
				}catch(Exception e){
					logger.error("删除文件时出错！" + e.getLocalizedMessage(), e);
				}
			}
		}else{
			info.setSuccess(false);
			info.setMessage("下载未尚完成，请等待!");
    		this.writeJsonResultToClient(response, info,0);
		}
		
	}

	public void downloadZipRemote(String url, int taskID, HttpServletRequest request, HttpServletResponse response){
		byte[] responseDataBuffer = new byte[2048];
		OutputStream out = null;
		InputStream in = null;
		try {
			String urlNameString = url + "?taskID=" + taskID;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", request.getHeader("user-agent"));
			// 建立实际的连接
			connection.connect();
			in = connection.getInputStream();
			out = response.getOutputStream();

			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", connection.getHeaderField("Content-Disposition"));

			int len = -1;
			while((len = in.read(responseDataBuffer)) != -1){
				out.write(responseDataBuffer, 0, len);
			}
		} catch (Exception e) {
			logger.error("发送GET请求出现异常！", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if(out != null){
					out.close();
				}
			} catch (Exception e2) {
				logger.error("关闭输入流异常！", e2);
			}
		}
	}
	
	/**
	 * 根据taskID获取下载进度情况
	 * @param taskID
	 * @return
	 */
	public ExpTaskInfo getTaskInfoByID(int taskID){
		ExpTaskInfo info = expTaskManager.getTaskByID(taskID);
		return TaskStatusChanger.updateTaskInfo(info);
	}
	
	/**
	 * 采用excel导出数据
	 * @param sql
	 * @param humanID
	 * @param hasImage
	 * @param resultCount
	 * @param splitExcel
	 * @param numPerPage
	 * @param request
	 * @param response
	 * @return
	 */
	private ResultInfo expDataByExcel(String sql, int expCount, int humanID, boolean hasImage,
			int splitExcel, int numPerPage, int imageType) throws SQLException {
		ResultInfo info = null;
		info = expTaskManager.addDownLoadTask(humanID, sql, hasImage, expCount, numPerPage, 1, imageType);
		return info;
	}
	
	/**
	 * 采用csv导出案件
	 * @param sql
	 * @param humanID
	 * @param resultCount
	 * @param splitExcel
	 * @param numPerPage
	 * @param request
	 * @param response
	 * @return
	 */
	private ResultInfo expDataByCSV(String sql, int expCount, int humanID, int splitExcel, int numPerPage) {
		ResultInfo info = null;
		info = expTaskManager.addDownLoadTask(humanID, sql, false, expCount, numPerPage, 2, 0);
		return info;
	}
	
	/**
	 * 获取文件下载的临时路径
	 * @param request
	 * @param taskID
	 * @param humanID
	 * @return
	 */
	private String getDownloadTempPath(HttpServletRequest request, String tempFilePath, int taskID, int humanID) {
		String tempUrl = null;
		if(request == null){
			tempUrl = tempFilePath;
		}else{
			tempUrl = request.getSession().getServletContext().getRealPath("/upfile");
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String dateStr = sdf.format(new Date());
		StringBuffer sb = new StringBuffer();
		sb.append(tempUrl);
		sb.append(File.separator);
		sb.append(dateStr);
		sb.append(File.separator);
		sb.append(taskID);
		sb.append(File.separator);
		sb.append(humanID);
		sb.append(File.separator);
		return sb.toString();
	}
	
	/**
	 * 获取文件下载的临时路径
	 * @param request
	 * @param taskID
	 * @param humanID
	 * @return
	 */
	private String getTaskTempPath(HttpServletRequest request, String tempFilePath) {
		String tempUrl = null;
		if(request == null){
			tempUrl = tempFilePath;
		}else{
			tempUrl = request.getSession().getServletContext().getRealPath("/upfile");
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String dateStr = sdf.format(new Date());
		StringBuffer sb = new StringBuffer();
		sb.append(tempUrl);
		sb.append(File.separator);
		sb.append(dateStr);
		return sb.toString();
	}
	
	/**
	 * 将ResultInfo类的变量转化为字符串
	 * @param info
	 * @return
	 */
	private String getJsonFromResultInfo(ResultInfo info){
		StringBuffer sb = new StringBuffer();
		StringBuffer dataSB = new StringBuffer();
		Map <String,Object> data = info.getData();
		Iterator iterator = data.keySet().iterator();
		while(iterator.hasNext()){
			String key = iterator.next().toString();
			String value = data.get(key).toString();
			dataSB.append("'"+ key +"':").append("'" + value+ "',");
		}
		if(dataSB.toString().length() != 0){
			dataSB.deleteCharAt(dataSB.length()-1);
		}
		sb.append("{");
		//sb.append("'flag':").append(info.getFlag()).append(",");
		sb.append("'message':").append("'" + info.getMessage() + "'").append(",");
		sb.append("'success':").append(info.isSuccess()).append(",");
		sb.append("'data': {");
        sb.append(dataSB.toString());		
		sb.append("}");
		sb.append("}");
		String result = sb.toString().replaceAll("'", "\"");
		return result;
	}
	
	/**
	 * 向客户端操作结果
	 * @param response
	 * @param info
	 */
	private void writeJsonResultToClient(HttpServletResponse response, ResultInfo info, int humanID) {
	    response.reset(); 
		JsonUtils.sendJson(response, info);
	}
	
	/**
	 * 获取导出案件的数量
	 * @param sql
	 * 
	 */
	public int getExportCount(String sql) {
		Session session = exportTaskDao.getSessionFactory().getCurrentSession();
		Query query = session.createSQLQuery(getCountSql(sql));
		List list = query.list();
		if ( list.size() > 0 ) {
			System.out.println("count: " + ((Number)list.get(0)).intValue() );
			return ((Number)list.get(0)).intValue();
		}
		return 0;
	}
	
	private String getCountSql(String sql) {
		int fromIndex = sql.indexOf("from");
		return "select count(*) " + sql.substring(fromIndex);
	}
	
	private int getSqlSelectFieldsCount(String sql) {
		int fromIndex = sql.indexOf("from");
		String string = sql.substring(0,fromIndex);
		return string.split(",").length;
	}
	
	/**
	 * 获取导出语句中的字段数量
	 * @param sql
	 * @param hasImage
	 * @param imageType
	 * @param imageNum
	 * @return
	 */
	public int getFieldLength(String sql, boolean hasImage, int imageType) {
		int count = getSqlSelectFieldsCount(sql);
		return hasImage ? count + 1 : count;
	}
	
	/**
	 * 获取任务列表信息
	 * @return
	 */
	public List<ExpTaskInfo> getTaskInfo() {
		return TaskStatusChanger.updateTaskInfo(expTaskManager.getTaskInfo());
	}
	
	public ResultInfo deleteTask(int taskID) {
		TaskStatusChanger.removeTaskInfo(taskID);
		TaskStatusMoniter.removeRunninTask(taskID);
		return expTaskManager.deleteTaskByID(taskID);
	}
	
	public ResultInfo exportData(String sql, boolean hasImage, int imageType, int humanID, String fileName, String imageFields, String imageServer) {
		initConf();
		ResultInfo info = new ResultInfo(true);
	    try {
	    	int splitExcel = 0;
		    int numPerPage = conf.getMaxExcelNoneImagePerPage();
		    sql = sql.replaceAll("''", "''''");
		    int fieldLength = this.getFieldLength(sql, hasImage, imageType);
		    int resultCount = this.getExportCount(sql);
		    
		    ExpTaskInfo taskInfo = new ExpTaskInfo();
	        taskInfo.setCreateTime(new Date());
	        taskInfo.setLastUpdateTime(taskInfo.getCreateTime());
	        taskInfo.setHumanID(humanID);
	        taskInfo.setQuerySQL(sql);
	        taskInfo.setFileName(fileName);
	        if ( hasImage ) {
	        	taskInfo.setHasImage(1);
	        	taskInfo.setImageType(imageType);
	        	taskInfo.setImageFields(imageFields);
	        	taskInfo.setImageServerUrl(imageServer);
	        } else {
	        	taskInfo.setHasImage(0);
	        }
	        taskInfo.setExpCount(resultCount);
	        taskInfo.setNumPerPage(numPerPage);
	        taskInfo.setDealServer(ServiceContext.hostName);
		    
		    if (fieldLength <= 255 && resultCount <= conf.getMaxExcel()) { // 导出数量小于10000条，采用excel导出
		        if (hasImage) {
		            if (resultCount > conf.getMaxExcelHasImagePerPage()) { // 含图片 500条一页
		       	        splitExcel = 1;
		        	}
		        } else {
		            if (resultCount > conf.getMaxExcelNoneImagePerPage()) { //不含图片2000条一页
		        	    splitExcel = 1;
		        	}
		        }
		        
		        try {
					info = this.expDataByExcel(taskInfo);
				} catch (Exception e) {
					logger.error("采用excel导出数据!" + e.getLocalizedMessage());
				}
		    } else {
		    	if (hasImage) { //导出超过含图片，且超过1万条
		            info.setSuccess(false);
		        	info.setMessage("导出包含图片且超过" + conf.getMaxExcel() + "条限制，易导致系统不稳定，请重新选择条件查询并导出!");
		        } else {
		            if (resultCount > conf.getMaxCsv()) { //导出数据大于 500000， 不予导出，返回提示
		                info.setSuccess(false);
		            	info.setMessage("导出数据超过" + conf.getMaxCsv() + "条限制，易导致系统不稳定，请重新选择条件查询并导出!");
		            } else {
		            	if (resultCount > conf.getMaxCsvPerPage()) {// 超过60000条分页
		            		splitExcel = 1;
		            	}
		            	numPerPage = conf.getMaxCsvPerPage();
		            	info = this.expDataByCSV(taskInfo);
		            }
		        }
		    }
		    info.addData("taskInfo", taskInfo);
	    } catch (Exception e) {
			info.setSuccess(false);
			info.setMessage("sql语句执行错误，请检查sql语句是否正确");
			e.printStackTrace();
		}
	    
	    return info;
	}

	private ResultInfo expDataByCSV(ExpTaskInfo taskInfo) {
		taskInfo.setFileStyle(2);
		return expTaskManager.addDownLoadTask(taskInfo);
	}

	private ResultInfo expDataByExcel(ExpTaskInfo taskInfo) {
		taskInfo.setFileStyle(1);
		return expTaskManager.addDownLoadTask(taskInfo);
	}

	/**
	 * 异步下载任务
	 * @param tempFilePath
	 * @param taskInfo
	 * @return
	 */
	public ResultInfo exportAsynTask(String tempFilePath, ExpTaskInfo taskInfo) {
		initConf();
		ResultInfo info = new ResultInfo();
		info.setSuccess(true);
		SqlData data = this.executeSql(taskInfo.getQuerySQL());
		if ( data.getCount() == 0 ) {
			info.setSuccess(false); 
			return info;
		}
		ViewData viewData = new ViewData(data);
		taskInfo.setExpCount(viewData.getData().size());
		if ( taskInfo.getHasImage() == 1 ) {
		    for ( ImageHandler handler : handlers ) {
		    	if ( handler.canHandle(taskInfo) ) {
		    		handler.handle(taskInfo, viewData, getServerUrl(taskInfo));
		    	} 
		    }
		}
		String tempPath = this.getDownloadTempPath(null, tempFilePath, taskInfo.getTaskID(), taskInfo.getHumanID());
		String taskPath = this.getTaskTempPath(null, tempFilePath);
		Map sysServer = null;//expTaskManager.getSMBFilePath();
		if ( taskInfo.getFileStyle() == 1) {
			info = excelTool.expMultiPageData(viewData.getData(), taskInfo.getHasImage() == 1, taskInfo.getNumPerPage(), taskInfo.getExpCount(), conf.getWinRarURL(), tempFilePath, taskInfo.getTaskID(), taskPath, tempPath, taskInfo.getImageType(), getColumnNames(viewData), sysServer,viewData,taskInfo);
		} else if ( taskInfo.getFileStyle() == 2 ) { 
			info = csvTool.expMultiPageData(data,taskInfo.getNumPerPage(), conf.getWinRarURL(), taskInfo.getTaskID(), taskPath, tempPath, sysServer,taskInfo);
		}
		return info;
	}
	
	private String getServerUrl(ExpTaskInfo info) {
		if ( info.getImageServerUrl() != null && !info.getImageServerUrl().equals("") ) {
			return info.getImageServerUrl();
		}
		return "";
	}
	
	private String[] getColumnNames(ViewData viewData) {
		List<ViewColumn> columns = viewData.getColumns();
		String[] filedNames = new String[columns.size()];
		int i = 0;
		for ( ViewColumn column : columns ) {
			filedNames[i++] = column.getName();
		}
		return filedNames;
	}

	@Override
	public ResultInfo exportBySql(String sql, int humanID, String fileName){
		try {
			return exportBySql(sql, false, null, humanID, fileName, null, null, null, null);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new ResultInfo(false);
	}

	@Override
	public ResultInfo exportBySql(String sql, boolean hasImage, Integer imageType, int humanID, String fileName,
								  String imageFields, String imageServerUrl, String imageServerUser, String imageServerPwd) throws UnsupportedEncodingException {
		// 使用配置的地址
		String expServiceUrl = SysConfigUtils.getSysConfigStrValue(ConfigItemNameConst.EXPORT_SERVICE_URL, "");
		if(!expServiceUrl.isEmpty()){
			StringBuilder param = new StringBuilder();
			param.append("exportSql=").append(URLEncoder.encode(sql, "UTF-8"))
					.append("&isHaveImage=").append(hasImage)
					.append("&imageType=").append(imageType)
					.append("&humanID=").append(humanID)
					.append("&fileName=").append(URLEncoder.encode(fileName, "UTF-8"))
					.append("&imageFields=").append(URLEncoder.encode(imageFields, "UTF-8"))
					.append("&imageServerUrl=").append(imageServerUrl)
					.append("&imageServerUser=").append(URLEncoder.encode(imageServerUser, "UTF-8"))
					.append("&imageServerPwd=").append(imageServerPwd)
					.append("&local=").append(true);
			String res = HttpUtils.sendPost(expServiceUrl + "/dataexp/exportapi", param.toString(), "UTF-8");
			return Tools.dispatchEgovaHttpResponse(res);
		} else {

			String imageServer = null;
			if (hasImage) {
				if (!Tools.isEmpty(imageServerUser) && !Tools.isEmpty(imageServerPwd) && !Tools.isEmpty(imageServerUrl)) {
					imageServer = "smb://" + imageServerUser + ":" + imageServerPwd + "@" + imageServerUrl;
				} else {
					imageServer = FileUtils.getCommonShareFilePath();
				}
			}

			SqlData sqlData = getSqlData(sql);
			if (sqlData.getCount() == 0) {
				return new ResultInfo(false, "查询结果为空");
			}
			ExpTaskInfo taskInfo = new ExpTaskInfo();
			taskInfo.setCreateTime(new Date());
			taskInfo.setLastUpdateTime(taskInfo.getCreateTime());
			taskInfo.setHumanID(humanID);
			taskInfo.setFileName(fileName);
			taskInfo.setHasImage(0);
			taskInfo.setExpCount(sqlData.getCount());
			taskInfo.setNumPerPage(getConf().getMaxExcelNoneImagePerPage());
			taskInfo.setDealServer(ServiceContext.hostName);
			taskInfo.setTaskID(getNewTaskID());

			return exportByListData(sqlData, taskInfo);
		}
	}

	@Override
	public ResultInfo exportData(String fileName, int humanID, String dataProviderName, Map<String, Object> providerParams, boolean withPicture) {
		// 使用配置的地址
		String expServiceUrl = SysConfigUtils.getSysConfigStrValue(ConfigItemNameConst.EXPORT_SERVICE_URL, "");
		if (!expServiceUrl.isEmpty()) {
			StringBuilder param = new StringBuilder();
			try {
				param.append("fileName=").append(URLEncoder.encode(fileName, "UTF-8"))
                        .append("&humanID=").append(humanID)
                        .append("&providerName=").append(dataProviderName)
                        .append("&humanID=").append(humanID)
                        .append("&withPic=").append(withPicture);
				for (Map.Entry<String, Object> stringObjectEntry : providerParams.entrySet()) {
					param.append("&").append(stringObjectEntry.getKey()).append("=").append(URLEncoder.encode(stringObjectEntry.getValue().toString(), "UTF-8"));
				}
			} catch (UnsupportedEncodingException e) {
				logger.error("导出请求生成失败!", e);
			}
			String res = HttpUtils.sendPost(expServiceUrl + "/dataexp/exportremote", param.toString(), "UTF-8");
			return Tools.dispatchEgovaHttpResponse(res);
		} else {
			return exportDataLocal(fileName, humanID, dataProviderName, providerParams, withPicture);
		}
	}

	public ResultInfo exportDataLocal(String fileName, int humanID, String dataProviderName, Map<String, Object>
			providerParams, boolean withPicture){
		ExportDataProvider dataProvider = getProvider(dataProviderName);
		if(dataProvider != null ) {
			Object result = dataProvider.getListData(providerParams);
			if(result == null){
				return new ResultInfo(false, "未获取到导出数据!");
			}
			if (dataProvider.getResultClassType() == List.class) {
				if (withPicture) {
					return exportByListDataWithPic((List) result, fileName, humanID);
				} else {
					return exportByListData((List) result, fileName, humanID);
				}
			} else if (dataProvider.getResultClassType() == SqlData.class && withPicture) {
				return exportByListDataWithPic((SqlData) result, fileName, humanID);
			}
		}
		return new ResultInfo(false, "未找到数据提供方法!");
	}

	public ResultInfo exportByListData(List<Map<String,Object>> valueList, String fileName, int humanID) {
		ResultInfo result = checkExportEnv();
		if(!result.isSuccess()){
			return result;
		}
		final SqlData sqlData = new SqlData(valueList);
		initConf();

		ExpTaskInfo taskInfo = new ExpTaskInfo();
		taskInfo.setCreateTime(new Date());
		taskInfo.setLastUpdateTime(taskInfo.getCreateTime());
		taskInfo.setHumanID(humanID);
		taskInfo.setFileName(fileName);
		taskInfo.setHasImage(0);
		taskInfo.setExpCount(sqlData.getCount());
		taskInfo.setNumPerPage(conf.getMaxExcelNoneImagePerPage());
		taskInfo.setDealServer(ServiceContext.hostName);
		
		if(HibernateTool.HIBERNATE_DIALECT_MYSQL.equals(HibernateTool.getHibernateDialect())){
			taskInfo.setTaskID(exportTaskDao.getIntegerAutoIncrement());
		}else{
			taskInfo.setTaskID(exportTaskDao.getColumnMaxIntegerValue("taskID") + 1);
		}

		return exportByListData(sqlData, taskInfo);
	}

	public ResultInfo exportByListData(final SqlData sqlData, ExpTaskInfo taskInfo){
		initConf();
		ResultInfo info = checkExportEnv();
		if(!info.isSuccess()){
			return info;
		}
		
		if (sqlData.getValueList().get(0).keySet().size() <= 255 && sqlData.getValueList().size() <= conf.getMaxExcel
				()) { // 导出数量小于10000条，采用excel导出
			taskInfo.setFileStyle(1);
		} else {

			if (sqlData.getValueList().size() > conf.getMaxCsv()) { //导出数据大于 500000， 不予导出，返回提示
//				throw new EGovaCommonException("导出数据过大!");
				info.setSuccess(false);
				info.setMessage("导出数据超过" + conf.getMaxCsv() + "条限制，易导致系统不稳定，请重新选择条件查询并导出!");
				return info;
			} else {
				taskInfo.setFileStyle(2);
				taskInfo.setNumPerPage(conf.getMaxCsvPerPage());
			}
		}

		final ExpTaskInfo persisTaskInfo = exportTaskDao.save(taskInfo);

		new Thread() {
			public void run() {
				exportInThread(sqlData, persisTaskInfo);
			}
		}.start();

		info.addData("taskInfo", persisTaskInfo);
		return info;
	}
	
	private void exportInThread(SqlData sqlData, ExpTaskInfo taskInfo) {
		initConf();
		ViewData viewData = new ViewData(sqlData);
		taskInfo.setExpCount(viewData.getData().size());
		
		if ( taskInfo.getHasImage() == 1 ) {
		    for ( ImageHandler handler : handlers ) {
		    	if ( handler.canHandle(taskInfo) ) {
		    		handler.handle(taskInfo, viewData, getServerUrl(taskInfo));
		    	} 
		    }
		} 
		
		String tempFilePath = ServiceContext.tempPath;
		String tempPath = this.getDownloadTempPath(null, tempFilePath, taskInfo.getTaskID(), taskInfo.getHumanID());
		String taskPath = this.getTaskTempPath(null, tempFilePath);
		Map sysServer = null;//expTaskManager.getSMBFilePath();
		 
		try {
			if ( taskInfo.getFileStyle() == 1) {
			    excelTool.expMultiPageData(viewData.getData(), taskInfo.getHasImage() == 1, taskInfo.getNumPerPage(), taskInfo.getExpCount(), conf.getWinRarURL(), tempFilePath, taskInfo.getTaskID(), taskPath, tempPath, taskInfo.getImageType(), getColumnNames(viewData), sysServer,  viewData,taskInfo);
			} else if ( taskInfo.getFileStyle() == 2 ) { 
				csvTool.expMultiPageData(sqlData,taskInfo.getNumPerPage(), conf.getWinRarURL(), taskInfo.getTaskID(), taskPath, tempPath, sysServer,taskInfo);
			}
			taskInfo.setStatus(ExpTaskInfo.ExportFinish);
		} catch (Exception e) {
			taskInfo.setStatus(ExpTaskInfo.ExportError);
		}
	    expTaskManager.save(taskInfo);
 
	}

	@Override
	public ResultInfo exportByListDataWithPic(
			List<Map<String, Object>> valueList, String fileName, int humanID) {
		ResultInfo result = checkExportEnv();
		if(!result.isSuccess()){
			return result;
		}
		return exportByListDataWithPic(new SqlData(valueList), fileName, humanID);
	}

	@Override
	public ResultInfo exportByListDataWithPic(
			final SqlData sqlData, String fileName,
			int humanID) {
		initConf();
		ResultInfo info = checkExportEnv();
		if(!info.isSuccess()){
			return info;
		}
		List<Map<String, Object>> valueList = sqlData.getValueList();
	    try { 
		    int numPerPage = conf.getMaxExcelNoneImagePerPage();
		    int splitExcel = 0; 
		    int fieldLength = valueList.get(0).keySet().size();
		    int resultCount = valueList.size();
		    
		    ExpTaskInfo taskInfo = new ExpTaskInfo();
	        taskInfo.setCreateTime(new Date());
	        taskInfo.setLastUpdateTime(taskInfo.getCreateTime());
	        taskInfo.setHumanID(humanID); 
	        taskInfo.setFileName(fileName);
	        
        	taskInfo.setHasImage(1);
        	taskInfo.setImageType(ImageHandler.CommonType);
        	taskInfo.setImageFields("图片");
        	taskInfo.setImageServerUrl(FileUtils.getCommonShareFilePath()); 
	        	
	        taskInfo.setExpCount(resultCount);
	        taskInfo.setNumPerPage(numPerPage);
	        taskInfo.setDealServer(ServiceContext.hostName);
		    
		    if (fieldLength <= 255 && resultCount <= conf.getMaxExcel()) { // 导出数量小于10000条，采用excel导出
		        try {
					info = this.expDataByExcel(taskInfo);
				} catch (Exception e) {
					logger.error("采用excel导出数据!" + e.getLocalizedMessage());
				}
		    }else if (fieldLength <= 255 && resultCount <= conf.getMaxCsv()){
				//修改导出CSV并含有图片时，之前是直接弹出box，出现导出包含图片。。。CSV不支持插入图片
				//原来的代码是支持导出图片的excel而不支持带图片的CSV，此时若要导出CSV，图片会变成图片的路径
				if (resultCount > conf.getMaxCsvPerPage()) {// 超过60000条分页
					splitExcel=1;
				}
				numPerPage = conf.getMaxCsvPerPage();
				info = this.expDataByCSV(taskInfo);
			}else {
		            info.setSuccess(false);
		        	info.setMessage("导出包含图片且超过" + conf.getMaxExcel() + "条限制，易导致系统不稳定，请重新选择条件查询并导出!");
		    }
		    info.addData("taskInfo", taskInfo);
		    
		    final ExpTaskInfo persisTaskInfo = exportTaskDao.save(taskInfo);
		    new Thread() {
		    	public void run() { 
		    		exportInThread(sqlData,persisTaskInfo);
		    	}
		    }.start();
	    } catch (Exception e) {
			info.setSuccess(false);
			info.setMessage("sql语句执行错误，请检查sql语句是否正确");
			logger.error("sql语句执行错误，请检查sql语句是否正确", e);
		}
	    
	    return info;
	}

	public SqlData getSqlData(String sql){
		return this.executeSql(sql);
	}

	public int getNewTaskID(){
		if(HibernateTool.HIBERNATE_DIALECT_MYSQL.equals(HibernateTool.getHibernateDialect())){
			return exportTaskDao.getIntegerAutoIncrement();
		}else{
			return exportTaskDao.getColumnMaxIntegerValue("taskID") + 1;
		}
	}
	
	public ResultInfo checkExportEnv(){
		ResultInfo result = new ResultInfo(false);
		result.setMessage("内存不足，请等待其他导出任务完成后再开始！");
		// 虚拟机已经占用了多少内存   如：初始值为 -Xms1500m  
		long totalMemory = Runtime.getRuntime().totalMemory();   
		// totalMemory 中剩余内存   
		long freeMemory = Runtime.getRuntime().freeMemory();   
		// 最大可以使用内存   如：-Xmx2500m
		long maxMemory = Runtime.getRuntime().maxMemory();
//		System.out.println("totalMemory:" + totalMemory);
//		System.out.println("freeMemory:" + freeMemory);
//		System.out.println("maxMemory:" + maxMemory);
		
		// 如果虚拟机还剩下30%以上的内存
		if((freeMemory/1024f)/(totalMemory/1024f) > 0.3){
			result.setMessage("");
			result.setSuccess(true);
			return result;
		}
		
		// 如果虚拟机还可以申请至少30%以上的内存
		if(((maxMemory - totalMemory)/1024f)/(maxMemory/1024f) > 0.3){
			result.setMessage("");
			result.setSuccess(true);
			return result;
		}
		
		logger.error(result.getMessage() + "freeMemory:" + freeMemory/1024f + " maxMemory - totalMemory:" + (maxMemory - totalMemory)/1024f);
		
		return result;
	}

	public SqlData executeSql(String sql) {
		//		Session session = getSessionFactory().getCurrentSession();
		//		ExportResultTransformer exportResultTransformer = new ExportResultTransformer();
		//		Query query = session.createSQLQuery(sql);
		//		query.setResultTransformer(exportResultTransformer);
		//		List<Map<String,Object>> aliasToValueMapList = query.list();
		//Pagination pagination = new Pagination(sql,1,Integer.MAX_VALUE, jdbcTemplate);
		List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql);
		String[] col = null;
		if (!mapList.isEmpty()) {
			Map<String, Object> map = mapList.get(0);
			col = map.keySet().toArray(new String[0]);
		}
		return new SqlData(mapList, col);
	}
}
```
![Alt](../../resouces/201903/houzi.png)

