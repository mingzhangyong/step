## excel文件数据导入到数据库

这个功能似乎还有点用，记录一下；

整体思路如下：
 ```mermaid 
excel文件  -->  整理excel表头以及表头与字段的对应关系 ， 因为通常是中文对bean文件字段  -->  通过ExcelUtils工具类解析成对应的bean问价
 ```
示例如下：
```java
private ResultInfo uploadGCPoint(String path, MultipartFile file) {
        ResultInfo info = new ResultInfo(true);
        if (!file.isEmpty()) {
            List<String> nameList = new ArrayList<>();
            nameList.add("生活垃圾收集点代码");
            nameList.add("市、区(县)");
            nameList.add("街道(乡镇)");
            nameList.add("居委会(村)");
            nameList.add("地址");
            nameList.add("俗称或单位名称");
            nameList.add("所有权归属分类");
            nameList.add("启用日期");
            nameList.add("日均运输量(kg)");
            nameList.add("垃圾房总面积分类");
            nameList.add("垃圾收集方式分类");
            nameList.add("收集点设施类型");
            nameList.add("垃圾来源分类");
            nameList.add("垃圾点日清次数(次)");
            nameList.add("垃圾运向分类");
            nameList.add("运营单位");

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("生活垃圾收集点代码", "facilityCode");
            map.put("市、区(县)", "districtName");
            map.put("街道(乡镇)", "streetName");
            map.put("居委会(村)", "communityName");
            map.put("地址", "address");
            map.put("俗称或单位名称", "facilityName");
            map.put("所有权归属分类", "ownership");
            map.put("启用日期", "openingDate");
            map.put("日均运输量(kg)", "averageDailyTraffic");
            map.put("垃圾房总面积分类", "garbageRoomArea");
            map.put("垃圾收集方式分类", "garbageCollectionType");
            map.put("收集点设施类型", "garbageFacilityType");
            map.put("垃圾来源分类", "garbageSourceType");
            map.put("垃圾点日清次数(次)", "dailyCleaningTimes");
            map.put("垃圾运向分类", "garbageTransportType");
            map.put("运营单位", "operationUnit");
            List<GCPointExcelBean> result =
                    ExcelTools.getExcelDataList(path, file, nameList, ROW_FOR_DATA, map, GCPointExcelBean.class);

            if (null != result) {
                //result 并不是收集点对应的mapping
                List<SaniFacilityGcPoint> completeResult = completeGcPoint(result);
                boolean saved = ExcelTools.saveExams(pointDao, completeResult);
                if (saved) {
                    info.setSuccess(true);
                } else {
                    info.setSuccess(false);
                }
            } else {
                info.setSuccess(false);
            }
        } else {
            info.setSuccess(false);
        }
        if (!info.isSuccess()) {
            info.setMessage("上传文件出错！");
        }
        return info;
    }
```
